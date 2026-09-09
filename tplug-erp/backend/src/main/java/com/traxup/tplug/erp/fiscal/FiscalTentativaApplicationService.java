package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalTentativaApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalTentativaApplicationService(JdbcTemplate jdbc,
                                             AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado abrir(UUID tenantId, UUID usuarioId, UUID correcaoId) {
        var existente = buscarPorCorrecao(tenantId, correcaoId);
        if (!existente.isEmpty()) return new Resultado(existente.getFirst(), true);

        Origem origem = jdbc.query("""
                SELECT c.id AS correcao_id, c.rejeicao_id, c.documento_id,
                       c.status AS correcao_status, r.status AS rejeicao_status,
                       d.solicitacao_id, d.filial_id, d.modelo, d.ambiente,
                       d.serie, d.numero, d.tipo_operacao, d.regime_tributario,
                       d.uf_destino, d.cfop, d.cst_icms, d.csosn,
                       d.valor_bruto, d.valor_desconto, d.valor_total
                FROM fiscal_rejeicao_correcoes c
                JOIN fiscal_rejeicoes r
                  ON r.tenant_id = c.tenant_id AND r.id = c.rejeicao_id
                JOIN fiscal_documentos d
                  ON d.tenant_id = c.tenant_id AND d.id = c.documento_id
                WHERE c.tenant_id = ? AND c.id = ?
                FOR UPDATE OF c, d
                """,
                (rs, n) -> new Origem(
                        rs.getObject("correcao_id", UUID.class),
                        rs.getObject("rejeicao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getString("correcao_status"), rs.getString("rejeicao_status"),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("modelo"), rs.getString("ambiente"),
                        rs.getObject("serie", Integer.class),
                        rs.getObject("numero", Long.class),
                        rs.getString("tipo_operacao"), rs.getString("regime_tributario"),
                        rs.getString("uf_destino"), rs.getString("cfop"),
                        rs.getString("cst_icms"), rs.getString("csosn"),
                        rs.getBigDecimal("valor_bruto"),
                        rs.getBigDecimal("valor_desconto"),
                        rs.getBigDecimal("valor_total")),
                tenantId, correcaoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Correcao fiscal nao encontrada para o tenant"));

        if (!"APLICADA".equals(origem.correcaoStatus())
                || !"CORRIGIDA".equals(origem.rejeicaoStatus())) {
            throw new IllegalArgumentException(
                    "Nova tentativa exige correcao APLICADA e rejeicao CORRIGIDA");
        }

        Tentativa anterior = jdbc.query("""
                SELECT id, numero FROM fiscal_tentativas_emissao
                WHERE tenant_id = ? AND documento_id = ?
                ORDER BY numero DESC LIMIT 1
                """, (rs, n) -> new Tentativa(
                        rs.getObject("id", UUID.class), rs.getInt("numero")),
                tenantId, origem.documentoId()).stream().findFirst().orElse(null);
        int numero = anterior == null ? 1 : anterior.numero() + 1;
        UUID tentativaId = UUID.nameUUIDFromBytes(
                (correcaoId + ":tentativa").getBytes(StandardCharsets.UTF_8));
        String hash = hashDocumento(origem);

        int inseridos = jdbc.update("""
                INSERT INTO fiscal_tentativas_emissao
                    (id, tenant_id, solicitacao_id, documento_id, rejeicao_id,
                     correcao_id, tentativa_anterior_id, numero, status,
                     hash_documento_corrigido)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CRIADA', ?)
                ON CONFLICT (tenant_id, correcao_id) DO NOTHING
                """, tentativaId, tenantId, origem.solicitacaoId(), origem.documentoId(),
                origem.rejeicaoId(), correcaoId, anterior == null ? null : anterior.id(),
                numero, hash);
        if (inseridos == 0) {
            return new Resultado(buscarPorCorrecao(tenantId, correcaoId).getFirst(), true);
        }

        vincularArtefato(tenantId, tentativaId, origem.documentoId(), "XML",
                "fiscal_documentos_xml", "xml_id");
        vincularArtefato(tenantId, tentativaId, origem.documentoId(), "ASSINATURA",
                "fiscal_documentos_assinaturas", "assinatura_id");
        vincularArtefato(tenantId, tentativaId, origem.documentoId(), "TRANSMISSAO",
                "fiscal_transmissoes", "transmissao_id");
        vincularArtefato(tenantId, tentativaId, origem.documentoId(), "PROCESSADO",
                "fiscal_documentos_processados", "processado_id");

        auditoria.registrar(tenantId, usuarioId, null, origem.filialId(),
                "ABRIR_NOVA_TENTATIVA_FISCAL", "FISCAL_TENTATIVA_EMISSAO",
                tentativaId, "documentoId=" + origem.documentoId() + ";rejeicaoId="
                        + origem.rejeicaoId() + ";correcaoId=" + correcaoId
                        + ";numero=" + numero + ";hashDocumento=" + hash);
        return new Resultado(buscarPorCorrecao(tenantId, correcaoId).getFirst(), false);
    }

    @Transactional(readOnly = true)
    public List<Resumo> listar(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT t.id, t.solicitacao_id, t.documento_id, t.rejeicao_id,
                       t.correcao_id, t.tentativa_anterior_id, t.numero, t.status,
                       t.hash_documento_corrigido, t.criada_em, t.iniciada_em,
                       t.concluida_em,
                       (SELECT count(*) FROM fiscal_tentativa_artefatos_anteriores a
                         WHERE a.tenant_id = t.tenant_id AND a.tentativa_id = t.id)
                           AS artefatos_superados
                FROM fiscal_tentativas_emissao t
                WHERE t.tenant_id = ? AND t.documento_id = ?
                ORDER BY t.numero DESC
                """, (rs, n) -> mapear(rs.getObject("id", UUID.class),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("rejeicao_id", UUID.class),
                        rs.getObject("correcao_id", UUID.class),
                        rs.getObject("tentativa_anterior_id", UUID.class),
                        rs.getInt("numero"), rs.getString("status"),
                        rs.getString("hash_documento_corrigido"),
                        rs.getTimestamp("criada_em"), rs.getTimestamp("iniciada_em"),
                        rs.getTimestamp("concluida_em"),
                        rs.getInt("artefatos_superados")), tenantId, documentoId);
    }

    private List<Resumo> buscarPorCorrecao(UUID tenantId, UUID correcaoId) {
        return jdbc.query("""
                SELECT t.id, t.solicitacao_id, t.documento_id, t.rejeicao_id,
                       t.correcao_id, t.tentativa_anterior_id, t.numero, t.status,
                       t.hash_documento_corrigido, t.criada_em, t.iniciada_em,
                       t.concluida_em,
                       (SELECT count(*) FROM fiscal_tentativa_artefatos_anteriores a
                         WHERE a.tenant_id = t.tenant_id AND a.tentativa_id = t.id)
                           AS artefatos_superados
                FROM fiscal_tentativas_emissao t
                WHERE t.tenant_id = ? AND t.correcao_id = ?
                """, (rs, n) -> mapear(rs.getObject("id", UUID.class),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("rejeicao_id", UUID.class),
                        rs.getObject("correcao_id", UUID.class),
                        rs.getObject("tentativa_anterior_id", UUID.class),
                        rs.getInt("numero"), rs.getString("status"),
                        rs.getString("hash_documento_corrigido"),
                        rs.getTimestamp("criada_em"), rs.getTimestamp("iniciada_em"),
                        rs.getTimestamp("concluida_em"),
                        rs.getInt("artefatos_superados")), tenantId, correcaoId);
    }

    private void vincularArtefato(UUID tenantId, UUID tentativaId, UUID documentoId,
                                  String tipo, String tabela, String coluna) {
        UUID vinculoId = UUID.nameUUIDFromBytes(
                (tentativaId + ":artefato:" + tipo).getBytes(StandardCharsets.UTF_8));
        String sql = """
                INSERT INTO fiscal_tentativa_artefatos_anteriores
                    (id, tenant_id, tentativa_id, tipo, %s, situacao)
                SELECT ?, ?, ?, ?, id, 'INVALIDADO_POR_CORRECAO'
                FROM %s WHERE tenant_id = ? AND documento_id = ?
                ON CONFLICT (tenant_id, tentativa_id, tipo) DO NOTHING
                """.formatted(coluna, tabela);
        jdbc.update(sql, vinculoId, tenantId, tentativaId, tipo, tenantId, documentoId);
    }

    static String materialHash(String modelo, String ambiente, Integer serie, Long numero,
                               String tipoOperacao, String regime, String uf, String cfop,
                               String cst, String csosn, BigDecimal bruto,
                               BigDecimal desconto, BigDecimal total) {
        return String.join("|", valor(modelo), valor(ambiente), valor(serie), valor(numero),
                valor(tipoOperacao), valor(regime), valor(uf), valor(cfop), valor(cst),
                valor(csosn), decimal(bruto), decimal(desconto), decimal(total));
    }

    private String hashDocumento(Origem o) {
        return sha256(materialHash(o.modelo(), o.ambiente(), o.serie(), o.numero(),
                o.tipoOperacao(), o.regime(), o.uf(), o.cfop(), o.cst(), o.csosn(),
                o.bruto(), o.desconto(), o.total()));
    }

    private static String valor(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private static String decimal(BigDecimal valor) {
        return valor == null ? "" : valor.stripTrailingZeros().toPlainString();
    }

    private String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    private Resumo mapear(UUID id, UUID solicitacaoId, UUID documentoId,
                          UUID rejeicaoId, UUID correcaoId, UUID anteriorId,
                          int numero, String status, String hash, Timestamp criada,
                          Timestamp iniciada, Timestamp concluida, int artefatos) {
        return new Resumo(id, solicitacaoId, documentoId, rejeicaoId, correcaoId,
                anteriorId, numero, status, hash, criada.toInstant(),
                instante(iniciada), instante(concluida), artefatos);
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    record Tentativa(UUID id, int numero) {}
    record Origem(UUID correcaoId, UUID rejeicaoId, UUID documentoId,
                  String correcaoStatus, String rejeicaoStatus, UUID solicitacaoId,
                  UUID filialId, String modelo, String ambiente, Integer serie,
                  Long numero, String tipoOperacao, String regime, String uf,
                  String cfop, String cst, String csosn, BigDecimal bruto,
                  BigDecimal desconto, BigDecimal total) {}

    public record Resumo(UUID id, UUID solicitacaoId, UUID documentoId,
                         UUID rejeicaoId, UUID correcaoId, UUID tentativaAnteriorId,
                         int numero, String status, String hashDocumentoCorrigido,
                         Instant criadaEm, Instant iniciadaEm, Instant concluidaEm,
                         int artefatosSuperados) {}
    public record Resultado(Resumo tentativa, boolean repetida) {}
}
