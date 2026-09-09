package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalXmlApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalXmlApplicationService(JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado gerar(UUID tenantId, UUID usuarioId, UUID documentoId) {
        var existente = buscar(tenantId, documentoId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        Documento documento = jdbc.query("""
                SELECT id, filial_id, pedido_venda_id, modelo, ambiente, serie, numero,
                       valor_bruto, valor_desconto, valor_total,
                       tipo_operacao, regime_tributario, uf_destino, cfop, cst_icms, csosn
                FROM fiscal_documentos WHERE tenant_id = ? AND id = ?
                """, (rs, n) -> new Documento(
                        rs.getObject("id", UUID.class), rs.getObject("filial_id", UUID.class),
                        rs.getObject("pedido_venda_id", UUID.class), rs.getString("modelo"),
                        rs.getString("ambiente"), rs.getObject("serie", Integer.class),
                        rs.getObject("numero", Long.class), rs.getBigDecimal("valor_bruto"),
                        rs.getBigDecimal("valor_desconto"), rs.getBigDecimal("valor_total"),
                        rs.getString("tipo_operacao"), rs.getString("regime_tributario"),
                        rs.getString("uf_destino"), rs.getString("cfop"),
                        rs.getString("cst_icms"), rs.getString("csosn")),
                tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Documento fiscal nao encontrado para o tenant"));
        if (documento.cfop() == null || (documento.cst() == null && documento.csosn() == null)) {
            throw new IllegalArgumentException("Regra fiscal deve ser aplicada antes do XML 1.2");
        }
        if (documento.serie() == null || documento.numero() == null) {
            throw new IllegalArgumentException("Documento fiscal deve ser numerado antes do XML 1.2");
        }
        if (!"HOMOLOGACAO".equals(documento.ambiente())) {
            throw new IllegalArgumentException("XML preparatorio permitido somente em HOMOLOGACAO");
        }

        List<Item> itens = jdbc.query("""
                SELECT codigo, descricao, ncm, unidade, quantidade, preco_unitario,
                       adicional_combo_unitario, desconto_valor, total_item
                FROM fiscal_solicitacao_itens i
                JOIN fiscal_documentos d
                  ON d.tenant_id = i.tenant_id AND d.solicitacao_id = i.solicitacao_id
                WHERE d.tenant_id = ? AND d.id = ?
                ORDER BY i.criado_em, i.id
                """, (rs, n) -> new Item(rs.getString("codigo"), rs.getString("descricao"),
                        rs.getString("ncm"), rs.getString("unidade"), rs.getBigDecimal("quantidade"),
                        rs.getBigDecimal("preco_unitario"), rs.getBigDecimal("adicional_combo_unitario"),
                        rs.getBigDecimal("desconto_valor"), rs.getBigDecimal("total_item")),
                tenantId, documentoId);
        if (itens.isEmpty()) throw new IllegalArgumentException("Documento fiscal sem itens de snapshot");

        String xml = escrever(documento, itens);
        String hash = sha256(xml);
        UUID xmlId = UUID.nameUUIDFromBytes((documentoId + ":xml:1.2").getBytes(StandardCharsets.UTF_8));
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos_xml
                    (id, tenant_id, documento_id, versao, conteudo, hash_sha256)
                VALUES (?, ?, ?, '1.2', ?, ?)
                ON CONFLICT (tenant_id, documento_id, versao) DO NOTHING
                """, xmlId, tenantId, documentoId, xml, hash);
        Resultado resultado = inseridos == 1
                ? new Resultado(xmlId, documentoId, "1.2", hash, false)
                : buscar(tenantId, documentoId).getFirst().comRepetida(true);
        if (inseridos == 1) {
            auditoria.registrar(tenantId, usuarioId, null, documento.filialId(),
                    "GERAR_XML_HOMOLOGACAO", "FISCAL_DOCUMENTO_XML", xmlId,
                    "documentoId=" + documentoId + ";versao=1.2;hash=" + hash);
        }
        return resultado;
    }

    private List<Resultado> buscar(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT id, documento_id, versao, hash_sha256
                FROM fiscal_documentos_xml WHERE tenant_id = ? AND documento_id = ? AND versao = '1.2'
                """, (rs, n) -> new Resultado(rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class), rs.getString("versao"),
                        rs.getString("hash_sha256"), false), tenantId, documentoId);
    }

    String escrever(Documento d, List<Item> itens) {
        try {
            StringWriter out = new StringWriter();
            var w = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("TraxUPFiscal");
            w.writeAttribute("versao", "1.2");
            elemento(w, "ambiente", d.ambiente()); elemento(w, "modelo", d.modelo());
            elemento(w, "serie", Integer.toString(d.serie()));
            elemento(w, "numero", Long.toString(d.numero()));
            elemento(w, "documentoId", d.id().toString());
            elemento(w, "filialId", d.filialId().toString());
            elemento(w, "pedidoVendaId", d.pedidoVendaId().toString());
            w.writeStartElement("tributacao");
            elemento(w, "tipoOperacao", d.tipoOperacao());
            elemento(w, "regimeTributario", d.regime());
            elemento(w, "ufDestino", d.uf());
            elemento(w, "cfop", d.cfop());
            if (d.cst() != null) elemento(w, "cstIcms", d.cst());
            if (d.csosn() != null) elemento(w, "csosn", d.csosn());
            w.writeEndElement();
            w.writeStartElement("itens");
            for (Item i : itens) {
                w.writeStartElement("item");
                elemento(w, "codigo", i.codigo()); elemento(w, "descricao", i.descricao());
                if (i.ncm() != null) elemento(w, "ncm", i.ncm());
                elemento(w, "unidade", i.unidade()); elemento(w, "quantidade", valor(i.quantidade()));
                elemento(w, "precoUnitario", valor(i.precoUnitario()));
                elemento(w, "adicional", valor(i.adicional())); elemento(w, "desconto", valor(i.desconto()));
                elemento(w, "total", valor(i.total())); w.writeEndElement();
            }
            w.writeEndElement();
            w.writeStartElement("totais");
            elemento(w, "bruto", valor(d.bruto())); elemento(w, "desconto", valor(d.desconto()));
            elemento(w, "total", valor(d.total())); w.writeEndElement();
            w.writeEndElement(); w.writeEndDocument(); w.close();
            return out.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Falha ao gerar XML fiscal preparatorio", e);
        }
    }

    private void elemento(javax.xml.stream.XMLStreamWriter w, String nome, String valor)
            throws XMLStreamException {
        w.writeStartElement(nome); w.writeCharacters(valor); w.writeEndElement();
    }

    private String valor(BigDecimal valor) { return valor.stripTrailingZeros().toPlainString(); }

    private String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record Documento(UUID id, UUID filialId, UUID pedidoVendaId, String modelo,
                             String ambiente, Integer serie, Long numero, BigDecimal bruto, BigDecimal desconto, BigDecimal total,
                             String tipoOperacao, String regime, String uf, String cfop,
                             String cst, String csosn) {}
    record Item(String codigo, String descricao, String ncm, String unidade,
                        BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal adicional,
                        BigDecimal desconto, BigDecimal total) {}
    public record Resultado(UUID xmlId, UUID documentoId, String versao, String hashSha256,
                            boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(xmlId, documentoId, versao, hashSha256, valor);
        }
    }
}
