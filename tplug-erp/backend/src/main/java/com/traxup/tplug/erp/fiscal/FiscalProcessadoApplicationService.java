package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalProcessadoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalProcessadoApplicationService(JdbcTemplate jdbc,
                                              AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado gerar(UUID tenantId, UUID usuarioId, UUID documentoId) {
        var existente = buscarInterno(tenantId, documentoId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        Origem origem = jdbc.query("""
                SELECT t.id AS transmissao_id, t.solicitacao_id, t.documento_id,
                       t.assinatura_id, t.status, t.codigo_resposta,
                       t.mensagem_resposta, t.protocolo, t.hash_resposta,
                       a.conteudo_assinado, d.filial_id
                FROM fiscal_transmissoes t
                JOIN fiscal_documentos_assinaturas a
                  ON a.tenant_id = t.tenant_id AND a.id = t.assinatura_id
                JOIN fiscal_documentos d
                  ON d.tenant_id = t.tenant_id AND d.id = t.documento_id
                WHERE t.tenant_id = ? AND t.documento_id = ?
                  AND t.ambiente = 'HOMOLOGACAO'
                  AND t.provedor = 'SIMULADO'
                  AND t.status = 'AUTORIZADO_SIMULADO'
                """, (rs, n) -> new Origem(
                        rs.getObject("transmissao_id", UUID.class),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("assinatura_id", UUID.class),
                        rs.getString("status"), rs.getString("codigo_resposta"),
                        rs.getString("mensagem_resposta"), rs.getString("protocolo"),
                        rs.getString("hash_resposta"), rs.getString("conteudo_assinado"),
                        rs.getObject("filial_id", UUID.class)),
                tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transmissao simulada autorizada nao encontrada para o documento e tenant"));

        String conteudo = escrever(origem);
        String hash = sha256(conteudo);
        UUID processadoId = UUID.nameUUIDFromBytes(
                (documentoId + ":processado:simulado:1.0").getBytes(StandardCharsets.UTF_8));
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos_processados
                    (id, tenant_id, solicitacao_id, documento_id, assinatura_id,
                     transmissao_id, ambiente, tipo, versao, conteudo, hash_sha256)
                VALUES (?, ?, ?, ?, ?, ?, 'HOMOLOGACAO', 'PROCESSADO_SIMULADO', '1.0', ?, ?)
                ON CONFLICT (tenant_id, transmissao_id) DO NOTHING
                """, processadoId, tenantId, origem.solicitacaoId(), documentoId,
                origem.assinaturaId(), origem.transmissaoId(), conteudo, hash);

        Resultado resultado = inseridos == 1
                ? new Resultado(processadoId, origem.solicitacaoId(), documentoId,
                    origem.transmissaoId(), origem.protocolo(), origem.status(),
                    "PROCESSADO_SIMULADO", "1.0", hash, conteudo, false)
                : buscarInterno(tenantId, documentoId).getFirst().comRepetida(true);
        if (inseridos == 1) {
            auditoria.registrar(tenantId, usuarioId, null, origem.filialId(),
                    "GERAR_XML_PROCESSADO_HOMOLOGACAO", "FISCAL_DOCUMENTO_PROCESSADO",
                    processadoId, "documentoId=" + documentoId + ";transmissaoId="
                            + origem.transmissaoId() + ";protocolo=" + origem.protocolo()
                            + ";tipo=PROCESSADO_SIMULADO;hash=" + hash);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Resultado buscar(UUID tenantId, UUID documentoId) {
        return buscarInterno(tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "XML processado simulado nao encontrado para o documento e tenant"));
    }

    private List<Resultado> buscarInterno(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT p.id, p.solicitacao_id, p.documento_id, p.transmissao_id,
                       t.protocolo, t.status, p.tipo, p.versao, p.hash_sha256, p.conteudo
                FROM fiscal_documentos_processados p
                JOIN fiscal_transmissoes t
                  ON t.tenant_id = p.tenant_id AND t.id = p.transmissao_id
                WHERE p.tenant_id = ? AND p.documento_id = ?
                  AND p.ambiente = 'HOMOLOGACAO'
                  AND p.tipo = 'PROCESSADO_SIMULADO'
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class), rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("transmissao_id", UUID.class), rs.getString("protocolo"),
                        rs.getString("status"), rs.getString("tipo"), rs.getString("versao"),
                        rs.getString("hash_sha256"), rs.getString("conteudo"), false),
                tenantId, documentoId);
    }

    String escrever(Origem o) {
        try {
            StringWriter out = new StringWriter();
            var w = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("TraxUPFiscalProcessado");
            w.writeAttribute("versao", "1.0");
            w.writeAttribute("tipo", "PROCESSADO_SIMULADO");
            elemento(w, "ambiente", "HOMOLOGACAO");
            elemento(w, "aviso", "Artefato simulado sem validade fiscal");
            w.writeStartElement("protocoloSimulado");
            elemento(w, "numero", o.protocolo());
            elemento(w, "status", o.status());
            elemento(w, "codigo", o.codigo());
            elemento(w, "mensagem", o.mensagem());
            elemento(w, "hashResposta", o.hashResposta());
            w.writeEndElement();
            w.writeStartElement("xmlAssinadoSimulado");
            w.writeCharacters(o.conteudoAssinado());
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndDocument();
            w.close();
            return out.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Falha ao gerar XML processado simulado", e);
        }
    }

    private void elemento(javax.xml.stream.XMLStreamWriter w, String nome, String valor)
            throws XMLStreamException {
        w.writeStartElement(nome);
        w.writeCharacters(valor);
        w.writeEndElement();
    }

    private String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    record Origem(UUID transmissaoId, UUID solicitacaoId, UUID documentoId,
                  UUID assinaturaId, String status, String codigo, String mensagem,
                  String protocolo, String hashResposta, String conteudoAssinado,
                  UUID filialId) {}

    public record Resultado(UUID processadoId, UUID solicitacaoId, UUID documentoId,
                            UUID transmissaoId, String protocolo, String status,
                            String tipo, String versao, String hashSha256,
                            String conteudo, boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(processadoId, solicitacaoId, documentoId, transmissaoId,
                    protocolo, status, tipo, versao, hashSha256, conteudo, valor);
        }
    }
}
