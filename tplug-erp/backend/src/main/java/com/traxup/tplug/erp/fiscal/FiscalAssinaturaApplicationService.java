package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalAssinaturaApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final FiscalAssinaturaPort assinador;

    public FiscalAssinaturaApplicationService(JdbcTemplate jdbc, AuditoriaApplicationService auditoria,
                                              FiscalAssinaturaPort assinador) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.assinador = assinador;
    }

    @Transactional
    public Resultado assinarHomologacao(UUID tenantId, UUID usuarioId, UUID documentoId) {
        var existente = buscar(tenantId, documentoId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        XmlFiscal xml = jdbc.query("""
                SELECT x.id, x.documento_id, x.versao, x.conteudo, d.filial_id, d.ambiente
                FROM fiscal_documentos_xml x
                JOIN fiscal_documentos d
                  ON d.tenant_id = x.tenant_id AND d.id = x.documento_id
                WHERE x.tenant_id = ? AND x.documento_id = ? AND x.versao = '1.2'
                """, (rs, n) -> new XmlFiscal(
                        rs.getObject("id", UUID.class), rs.getObject("documento_id", UUID.class),
                        rs.getString("versao"), rs.getString("conteudo"),
                        rs.getObject("filial_id", UUID.class), rs.getString("ambiente")),
                tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "XML fiscal 1.2 nao encontrado para o documento e tenant"));
        if (!"HOMOLOGACAO".equals(xml.ambiente())) {
            throw new IllegalArgumentException("Assinatura simulada permitida somente em HOMOLOGACAO");
        }

        Certificado certificado = jdbc.query("""
                SELECT id, thumbprint_sha256
                FROM fiscal_certificados_digitais
                WHERE tenant_id = ? AND filial_id = ? AND ativo = TRUE
                  AND validade_inicio <= CURRENT_TIMESTAMP AND validade_fim > CURRENT_TIMESTAMP
                """, (rs, n) -> new Certificado(rs.getObject("id", UUID.class),
                        rs.getString("thumbprint_sha256")), tenantId, xml.filialId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Certificado fiscal ativo e valido nao encontrado para a filial"));

        var assinatura = assinador.assinar(new FiscalAssinaturaPort.Comando(
                documentoId, certificado.id(), certificado.thumbprint(), xml.conteudo()));
        UUID assinaturaId = UUID.nameUUIDFromBytes(
                (documentoId + ":assinatura:simulada:1.2").getBytes(StandardCharsets.UTF_8));
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos_assinaturas
                    (id, tenant_id, documento_id, xml_id, certificado_id, versao_xml,
                     ambiente, tipo_assinatura, algoritmo, conteudo_assinado, hash_sha256)
                VALUES (?, ?, ?, ?, ?, '1.2', 'HOMOLOGACAO', ?, ?, ?, ?)
                ON CONFLICT (tenant_id, xml_id) DO NOTHING
                """, assinaturaId, tenantId, documentoId, xml.id(), certificado.id(),
                assinatura.tipo(), assinatura.algoritmo(), assinatura.conteudoAssinado(),
                assinatura.hashSha256());

        Resultado resultado = inseridos == 1
                ? new Resultado(assinaturaId, documentoId, xml.id(), certificado.id(),
                    assinatura.tipo(), assinatura.algoritmo(), assinatura.hashSha256(), false)
                : buscar(tenantId, documentoId).getFirst().comRepetida(true);
        if (inseridos == 1) {
            auditoria.registrar(tenantId, usuarioId, null, xml.filialId(),
                    "ASSINAR_XML_HOMOLOGACAO_SIMULADA", "FISCAL_DOCUMENTO_ASSINATURA",
                    assinaturaId, "documentoId=" + documentoId + ";xmlId=" + xml.id()
                            + ";tipo=SIMULADA;hash=" + assinatura.hashSha256());
        }
        return resultado;
    }

    private List<Resultado> buscar(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT id, documento_id, xml_id, certificado_id, tipo_assinatura,
                       algoritmo, hash_sha256
                FROM fiscal_documentos_assinaturas
                WHERE tenant_id = ? AND documento_id = ?
                  AND ambiente = 'HOMOLOGACAO' AND tipo_assinatura = 'SIMULADA'
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class), rs.getObject("documento_id", UUID.class),
                        rs.getObject("xml_id", UUID.class), rs.getObject("certificado_id", UUID.class),
                        rs.getString("tipo_assinatura"), rs.getString("algoritmo"),
                        rs.getString("hash_sha256"), false), tenantId, documentoId);
    }

    record XmlFiscal(UUID id, UUID documentoId, String versao, String conteudo,
                     UUID filialId, String ambiente) {}
    record Certificado(UUID id, String thumbprint) {}

    public record Resultado(UUID assinaturaId, UUID documentoId, UUID xmlId, UUID certificadoId,
                            String tipo, String algoritmo, String hashSha256, boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(assinaturaId, documentoId, xmlId, certificadoId,
                    tipo, algoritmo, hashSha256, valor);
        }
    }
}
