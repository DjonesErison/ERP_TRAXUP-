package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalTentativaProcessadoApplicationService {
    private final JdbcTemplate jdbc;
    private final FiscalProcessadoApplicationService envelope;
    private final AuditoriaApplicationService auditoria;

    public FiscalTentativaProcessadoApplicationService(
            JdbcTemplate jdbc,
            FiscalProcessadoApplicationService envelope,
            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.envelope = envelope;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado gerar(UUID tenantId, UUID usuarioId, UUID tentativaId) {
        var existente = buscarInterno(tenantId, tentativaId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        Origem o = jdbc.query("""
                SELECT te.numero, te.status AS tentativa_status, te.solicitacao_id,
                       te.documento_id, d.filial_id, tr.id AS transmissao_id,
                       tr.assinatura_id, tr.ambiente, tr.provedor,
                       tr.status AS transmissao_status, tr.codigo_resposta,
                       tr.mensagem_resposta, tr.protocolo, tr.hash_resposta,
                       a.conteudo_assinado
                FROM fiscal_tentativas_emissao te
                JOIN fiscal_documentos d
                  ON d.tenant_id = te.tenant_id AND d.id = te.documento_id
                JOIN fiscal_transmissoes tr
                  ON tr.tenant_id = te.tenant_id AND tr.tentativa_id = te.id
                JOIN fiscal_documentos_assinaturas a
                  ON a.tenant_id = te.tenant_id AND a.id = tr.assinatura_id
                   AND a.tentativa_id = te.id
                WHERE te.tenant_id = ? AND te.id = ?
                """, (rs, n) -> new Origem(
                        rs.getInt("numero"), rs.getString("tentativa_status"),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getObject("transmissao_id", UUID.class),
                        rs.getObject("assinatura_id", UUID.class),
                        rs.getString("ambiente"), rs.getString("provedor"),
                        rs.getString("transmissao_status"),
                        rs.getString("codigo_resposta"),
                        rs.getString("mensagem_resposta"),
                        rs.getString("protocolo"), rs.getString("hash_resposta"),
                        rs.getString("conteudo_assinado")),
                tenantId, tentativaId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Transmissao da tentativa nao encontrada para o tenant"));

        validar(o.tentativaStatus(), o.ambiente(), o.provedor(), o.transmissaoStatus());
        String conteudo = envelope.escrever(new FiscalProcessadoApplicationService.Origem(
                o.transmissaoId(), o.solicitacaoId(), o.documentoId(), o.assinaturaId(),
                o.transmissaoStatus(), o.codigo(), o.mensagem(), o.protocolo(),
                o.hashResposta(), o.conteudoAssinado(), o.filialId()));
        String hash = sha256(conteudo);
        UUID id = UUID.nameUUIDFromBytes((tentativaId + ":processado:simulado:1.0")
                .getBytes(StandardCharsets.UTF_8));

        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos_processados
                  (id, tenant_id, solicitacao_id, documento_id, assinatura_id,
                   transmissao_id, tentativa_id, ambiente, tipo, versao, conteudo,
                   hash_sha256)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'HOMOLOGACAO',
                        'PROCESSADO_SIMULADO', '1.0', ?, ?)
                ON CONFLICT DO NOTHING
                """, id, tenantId, o.solicitacaoId(), o.documentoId(), o.assinaturaId(),
                o.transmissaoId(), tentativaId, conteudo, hash);

        if (inseridos == 0) return buscarInterno(tenantId, tentativaId).stream()
                .findFirst().map(r -> r.comRepetida(true)).orElseThrow();

        auditoria.registrar(tenantId, usuarioId, null, o.filialId(),
                "GERAR_XML_PROCESSADO_TENTATIVA", "FISCAL_DOCUMENTO_PROCESSADO", id,
                "documentoId=" + o.documentoId() + ";tentativaId=" + tentativaId
                        + ";numero=" + o.numero() + ";transmissaoId=" + o.transmissaoId()
                        + ";protocolo=" + o.protocolo() + ";hash=" + hash);
        return new Resultado(id, o.documentoId(), tentativaId, o.numero(),
                o.transmissaoId(), o.protocolo(), o.transmissaoStatus(),
                "PROCESSADO_SIMULADO", "1.0", hash, conteudo, false);
    }

    @Transactional(readOnly = true)
    public Resultado buscar(UUID tenantId, UUID tentativaId) {
        return buscarInterno(tenantId, tentativaId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "XML processado da tentativa nao encontrado para o tenant"));
    }

    private List<Resultado> buscarInterno(UUID tenantId, UUID tentativaId) {
        return jdbc.query("""
                SELECT p.id, p.documento_id, p.tentativa_id, te.numero,
                       p.transmissao_id, tr.protocolo, tr.status, p.tipo,
                       p.versao, p.hash_sha256, p.conteudo
                FROM fiscal_documentos_processados p
                JOIN fiscal_tentativas_emissao te
                  ON te.tenant_id = p.tenant_id AND te.id = p.tentativa_id
                JOIN fiscal_transmissoes tr
                  ON tr.tenant_id = p.tenant_id AND tr.id = p.transmissao_id
                WHERE p.tenant_id = ? AND p.tentativa_id = ?
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("tentativa_id", UUID.class), rs.getInt("numero"),
                        rs.getObject("transmissao_id", UUID.class),
                        rs.getString("protocolo"), rs.getString("status"),
                        rs.getString("tipo"), rs.getString("versao"),
                        rs.getString("hash_sha256"), rs.getString("conteudo"), false),
                tenantId, tentativaId);
    }

    private String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    static void validar(String tentativaStatus, String ambiente, String provedor,
                        String transmissaoStatus) {
        if (!"CONCLUIDA".equals(tentativaStatus))
            throw new IllegalArgumentException("XML processado exige tentativa CONCLUIDA");
        if (!"HOMOLOGACAO".equals(ambiente) || !"SIMULADO".equals(provedor)
                || !"AUTORIZADO_SIMULADO".equals(transmissaoStatus))
            throw new IllegalArgumentException(
                    "XML processado exige transmissao simulada autorizada em homologacao");
    }

    record Origem(int numero, String tentativaStatus, UUID solicitacaoId,
                  UUID documentoId, UUID filialId, UUID transmissaoId,
                  UUID assinaturaId, String ambiente, String provedor,
                  String transmissaoStatus, String codigo, String mensagem,
                  String protocolo, String hashResposta, String conteudoAssinado) {}

    public record Resultado(UUID processadoId, UUID documentoId, UUID tentativaId,
                            int tentativaNumero, UUID transmissaoId, String protocolo,
                            String status, String tipo, String versao, String hashSha256,
                            String conteudo, boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(processadoId, documentoId, tentativaId, tentativaNumero,
                    transmissaoId, protocolo, status, tipo, versao, hashSha256,
                    conteudo, valor);
        }
    }
}
