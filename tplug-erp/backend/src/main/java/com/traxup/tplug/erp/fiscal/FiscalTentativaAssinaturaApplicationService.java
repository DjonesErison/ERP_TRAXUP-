package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalTentativaAssinaturaApplicationService {
    private final JdbcTemplate jdbc;
    private final FiscalAssinaturaPort assinador;
    private final AuditoriaApplicationService auditoria;

    public FiscalTentativaAssinaturaApplicationService(
            JdbcTemplate jdbc, FiscalAssinaturaPort assinador,
            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.assinador = assinador;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado assinar(UUID tenantId, UUID usuarioId, UUID tentativaId) {
        var existente = buscar(tenantId, tentativaId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        Origem origem = jdbc.query("""
                SELECT t.id AS tentativa_id, t.numero AS tentativa_numero,
                       t.status AS tentativa_status, t.documento_id,
                       x.id AS xml_id, x.conteudo AS xml_conteudo,
                       x.versao AS xml_versao, d.filial_id, d.ambiente
                FROM fiscal_tentativas_emissao t
                JOIN fiscal_documentos d
                  ON d.tenant_id = t.tenant_id AND d.id = t.documento_id
                JOIN fiscal_documentos_xml x
                  ON x.tenant_id = t.tenant_id AND x.tentativa_id = t.id
                WHERE t.tenant_id = ? AND t.id = ? AND x.versao = '1.2'
                FOR UPDATE OF t
                """, (rs, n) -> new Origem(
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getInt("tentativa_numero"), rs.getString("tentativa_status"),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("xml_id", UUID.class), rs.getString("xml_conteudo"),
                        rs.getString("xml_versao"),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("ambiente")), tenantId, tentativaId)
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "XML 1.2 da tentativa nao encontrado para o tenant"));

        validar(origem.status(), origem.ambiente(), origem.versao());

        Certificado certificado = jdbc.query("""
                SELECT id, thumbprint_sha256
                FROM fiscal_certificados_digitais
                WHERE tenant_id = ? AND filial_id = ? AND ativo = TRUE
                  AND validade_inicio <= CURRENT_TIMESTAMP
                  AND validade_fim > CURRENT_TIMESTAMP
                ORDER BY atualizado_em DESC
                LIMIT 1
                """, (rs, n) -> new Certificado(
                        rs.getObject("id", UUID.class),
                        rs.getString("thumbprint_sha256")),
                tenantId, origem.filialId()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Certificado fiscal ativo e valido nao encontrado para a filial"));

        var assinatura = assinador.assinar(new FiscalAssinaturaPort.Comando(
                origem.documentoId(), certificado.id(), certificado.thumbprint(),
                origem.conteudoXml()));
        UUID assinaturaId = UUID.nameUUIDFromBytes(
                (tentativaId + ":assinatura:simulada:1.2")
                        .getBytes(StandardCharsets.UTF_8));
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos_assinaturas
                    (id, tenant_id, documento_id, xml_id, certificado_id,
                     tentativa_id, versao_xml, ambiente, tipo_assinatura,
                     algoritmo, conteudo_assinado, hash_sha256)
                VALUES (?, ?, ?, ?, ?, ?, '1.2', 'HOMOLOGACAO', ?, ?, ?, ?)
                ON CONFLICT DO NOTHING
                """, assinaturaId, tenantId, origem.documentoId(), origem.xmlId(),
                certificado.id(), tentativaId, assinatura.tipo(),
                assinatura.algoritmo(), assinatura.conteudoAssinado(),
                assinatura.hashSha256());
        if (inseridos == 0) {
            return buscar(tenantId, tentativaId).stream().findFirst()
                    .map(r -> r.comRepetida(true))
                    .orElseThrow(() -> new IllegalStateException(
                            "Conflito ao persistir assinatura da tentativa"));
        }

        auditoria.registrar(tenantId, usuarioId, null, origem.filialId(),
                "ASSINAR_XML_TENTATIVA_FISCAL", "FISCAL_DOCUMENTO_ASSINATURA",
                assinaturaId, "documentoId=" + origem.documentoId()
                        + ";tentativaId=" + tentativaId + ";tentativaNumero="
                        + origem.numeroTentativa() + ";xmlId=" + origem.xmlId()
                        + ";tipo=SIMULADA;hash=" + assinatura.hashSha256());
        return new Resultado(assinaturaId, origem.documentoId(), tentativaId,
                origem.numeroTentativa(), origem.xmlId(), certificado.id(),
                assinatura.tipo(), assinatura.algoritmo(),
                assinatura.hashSha256(), false);
    }

    private List<Resultado> buscar(UUID tenantId, UUID tentativaId) {
        return jdbc.query("""
                SELECT a.id, a.documento_id, a.tentativa_id, t.numero,
                       a.xml_id, a.certificado_id, a.tipo_assinatura,
                       a.algoritmo, a.hash_sha256
                FROM fiscal_documentos_assinaturas a
                JOIN fiscal_tentativas_emissao t
                  ON t.tenant_id = a.tenant_id AND t.id = a.tentativa_id
                WHERE a.tenant_id = ? AND a.tentativa_id = ?
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getInt("numero"), rs.getObject("xml_id", UUID.class),
                        rs.getObject("certificado_id", UUID.class),
                        rs.getString("tipo_assinatura"),
                        rs.getString("algoritmo"), rs.getString("hash_sha256"),
                        false), tenantId, tentativaId);
    }

    static void validar(String status, String ambiente, String versao) {
        if (!"EM_PROCESSAMENTO".equals(status)) {
            throw new IllegalArgumentException(
                    "Assinatura exige tentativa EM_PROCESSAMENTO");
        }
        if (!"HOMOLOGACAO".equals(ambiente)) {
            throw new IllegalArgumentException(
                    "Assinatura simulada permitida somente em HOMOLOGACAO");
        }
        if (!"1.2".equals(versao)) {
            throw new IllegalArgumentException(
                    "Assinatura da tentativa exige XML 1.2");
        }
    }

    record Origem(UUID tentativaId, int numeroTentativa, String status,
                  UUID documentoId, UUID xmlId, String conteudoXml,
                  String versao, UUID filialId, String ambiente) {}
    record Certificado(UUID id, String thumbprint) {}

    public record Resultado(UUID assinaturaId, UUID documentoId,
                            UUID tentativaId, int tentativaNumero, UUID xmlId,
                            UUID certificadoId, String tipo, String algoritmo,
                            String hashSha256, boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(assinaturaId, documentoId, tentativaId,
                    tentativaNumero, xmlId, certificadoId, tipo, algoritmo,
                    hashSha256, valor);
        }
    }
}
