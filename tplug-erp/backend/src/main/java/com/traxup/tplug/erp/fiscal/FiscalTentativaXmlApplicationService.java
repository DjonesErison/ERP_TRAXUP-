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
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalTentativaXmlApplicationService {
    private final JdbcTemplate jdbc;
    private final FiscalXmlApplicationService geradorXml;
    private final AuditoriaApplicationService auditoria;

    public FiscalTentativaXmlApplicationService(JdbcTemplate jdbc,
                                                FiscalXmlApplicationService geradorXml,
                                                AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.geradorXml = geradorXml;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado gerar(UUID tenantId, UUID usuarioId, UUID tentativaId) {
        var existente = buscar(tenantId, tentativaId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        Origem origem = jdbc.query("""
                SELECT t.id AS tentativa_id, t.documento_id, t.numero AS tentativa_numero,
                       t.status AS tentativa_status, t.hash_documento_corrigido,
                       d.filial_id, d.pedido_venda_id, d.modelo, d.ambiente,
                       d.serie, d.numero, d.valor_bruto, d.valor_desconto, d.valor_total,
                       d.tipo_operacao, d.regime_tributario, d.uf_destino,
                       d.cfop, d.cst_icms, d.csosn
                FROM fiscal_tentativas_emissao t
                JOIN fiscal_documentos d
                  ON d.tenant_id = t.tenant_id AND d.id = t.documento_id
                WHERE t.tenant_id = ? AND t.id = ?
                FOR UPDATE OF t, d
                """, (rs, n) -> new Origem(
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getInt("tentativa_numero"), rs.getString("tentativa_status"),
                        rs.getString("hash_documento_corrigido"),
                        rs.getObject("filial_id", UUID.class),
                        rs.getObject("pedido_venda_id", UUID.class),
                        rs.getString("modelo"), rs.getString("ambiente"),
                        rs.getObject("serie", Integer.class),
                        rs.getObject("numero", Long.class),
                        rs.getBigDecimal("valor_bruto"), rs.getBigDecimal("valor_desconto"),
                        rs.getBigDecimal("valor_total"), rs.getString("tipo_operacao"),
                        rs.getString("regime_tributario"), rs.getString("uf_destino"),
                        rs.getString("cfop"), rs.getString("cst_icms"),
                        rs.getString("csosn")), tenantId, tentativaId)
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tentativa fiscal nao encontrada para o tenant"));

        if (!"CRIADA".equals(origem.status()) && !"EM_PROCESSAMENTO".equals(origem.status())) {
            throw new IllegalArgumentException(
                    "XML so pode ser gerado para tentativa CRIADA ou EM_PROCESSAMENTO");
        }
        if (!"HOMOLOGACAO".equals(origem.ambiente())) {
            throw new IllegalArgumentException(
                    "Regeneracao preparatoria permitida somente em HOMOLOGACAO");
        }
        if (origem.serie() == null || origem.numeroDocumento() == null) {
            throw new IllegalArgumentException("Documento corrigido deve permanecer numerado");
        }
        if (origem.cfop() == null || (origem.cst() == null && origem.csosn() == null)) {
            throw new IllegalArgumentException("Documento corrigido ainda possui regra fiscal incompleta");
        }

        String hashAtual = calcularHashDocumento(origem.modelo(), origem.ambiente(),
                origem.serie(), origem.numeroDocumento(), origem.tipoOperacao(),
                origem.regime(), origem.uf(), origem.cfop(), origem.cst(), origem.csosn(),
                origem.bruto(), origem.desconto(), origem.total());
        if (!hashAtual.equals(origem.hashEsperado())) {
            throw new IllegalStateException(
                    "Documento fiscal mudou apos a abertura da tentativa");
        }

        List<FiscalXmlApplicationService.Item> itens = jdbc.query("""
                SELECT i.codigo, i.descricao, i.ncm, i.unidade, i.quantidade,
                       i.preco_unitario, i.adicional_combo_unitario,
                       i.desconto_valor, i.total_item
                FROM fiscal_solicitacao_itens i
                JOIN fiscal_documentos d
                  ON d.tenant_id = i.tenant_id AND d.solicitacao_id = i.solicitacao_id
                WHERE d.tenant_id = ? AND d.id = ?
                ORDER BY i.criado_em, i.id
                """, (rs, n) -> new FiscalXmlApplicationService.Item(
                        rs.getString("codigo"), rs.getString("descricao"),
                        rs.getString("ncm"), rs.getString("unidade"),
                        rs.getBigDecimal("quantidade"), rs.getBigDecimal("preco_unitario"),
                        rs.getBigDecimal("adicional_combo_unitario"),
                        rs.getBigDecimal("desconto_valor"), rs.getBigDecimal("total_item")),
                tenantId, origem.documentoId());
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Documento fiscal sem itens de snapshot");
        }

        var documento = new FiscalXmlApplicationService.Documento(
                origem.documentoId(), origem.filialId(), origem.pedidoVendaId(),
                origem.modelo(), origem.ambiente(), origem.serie(),
                origem.numeroDocumento(), origem.bruto(), origem.desconto(),
                origem.total(), origem.tipoOperacao(), origem.regime(),
                origem.uf(), origem.cfop(), origem.cst(), origem.csosn());
        String xml = geradorXml.escrever(documento, itens);
        String hashXml = sha256(xml);
        UUID xmlId = UUID.nameUUIDFromBytes(
                (tentativaId + ":xml:1.2").getBytes(StandardCharsets.UTF_8));

        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos_xml
                    (id, tenant_id, documento_id, tentativa_id, versao,
                     conteudo, hash_sha256)
                VALUES (?, ?, ?, ?, '1.2', ?, ?)
                ON CONFLICT DO NOTHING
                """, xmlId, tenantId, origem.documentoId(), tentativaId, xml, hashXml);
        if (inseridos == 0) {
            return buscar(tenantId, tentativaId).stream().findFirst()
                    .map(r -> r.comRepetida(true))
                    .orElseThrow(() -> new IllegalStateException(
                            "Conflito ao persistir XML da tentativa"));
        }

        if ("CRIADA".equals(origem.status())) {
            jdbc.update("""
                    UPDATE fiscal_tentativas_emissao
                    SET status = 'EM_PROCESSAMENTO', iniciada_em = CURRENT_TIMESTAMP
                    WHERE tenant_id = ? AND id = ? AND status = 'CRIADA'
                    """, tenantId, tentativaId);
        }
        auditoria.registrar(tenantId, usuarioId, null, origem.filialId(),
                "REGENERAR_XML_TENTATIVA_FISCAL", "FISCAL_DOCUMENTO_XML", xmlId,
                "documentoId=" + origem.documentoId() + ";tentativaId=" + tentativaId
                        + ";tentativaNumero=" + origem.tentativaNumero()
                        + ";versao=1.2;hash=" + hashXml);
        return new Resultado(xmlId, origem.documentoId(), tentativaId,
                origem.tentativaNumero(), "1.2", hashXml, false);
    }

    private List<Resultado> buscar(UUID tenantId, UUID tentativaId) {
        return jdbc.query("""
                SELECT x.id, x.documento_id, x.tentativa_id, t.numero,
                       x.versao, x.hash_sha256
                FROM fiscal_documentos_xml x
                JOIN fiscal_tentativas_emissao t
                  ON t.tenant_id = x.tenant_id AND t.id = x.tentativa_id
                WHERE x.tenant_id = ? AND x.tentativa_id = ? AND x.versao = '1.2'
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getInt("numero"), rs.getString("versao"),
                        rs.getString("hash_sha256"), false), tenantId, tentativaId);
    }

    static String calcularHashDocumento(String modelo, String ambiente, Integer serie,
                                        Long numero, String tipoOperacao, String regime,
                                        String uf, String cfop, String cst, String csosn,
                                        BigDecimal bruto, BigDecimal desconto,
                                        BigDecimal total) {
        return sha256(FiscalTentativaApplicationService.materialHash(
                modelo, ambiente, serie, numero, tipoOperacao, regime, uf, cfop,
                cst, csosn, bruto, desconto, total));
    }

    private static String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record Origem(UUID tentativaId, UUID documentoId, int tentativaNumero,
                  String status, String hashEsperado, UUID filialId,
                  UUID pedidoVendaId, String modelo, String ambiente, Integer serie,
                  Long numeroDocumento, BigDecimal bruto, BigDecimal desconto,
                  BigDecimal total, String tipoOperacao, String regime, String uf,
                  String cfop, String cst, String csosn) {}

    public record Resultado(UUID xmlId, UUID documentoId, UUID tentativaId,
                            int tentativaNumero, String versao, String hashSha256,
                            boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(xmlId, documentoId, tentativaId, tentativaNumero,
                    versao, hashSha256, valor);
        }
    }
}
