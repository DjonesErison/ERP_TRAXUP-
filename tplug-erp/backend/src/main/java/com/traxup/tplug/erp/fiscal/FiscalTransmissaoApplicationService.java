package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalTransmissaoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final FiscalTransmissaoPort transmissor;

    public FiscalTransmissaoApplicationService(JdbcTemplate jdbc,
                                               AuditoriaApplicationService auditoria,
                                               FiscalTransmissaoPort transmissor) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.transmissor = transmissor;
    }

    @Transactional
    public Resultado transmitirHomologacao(UUID tenantId, UUID usuarioId, UUID documentoId) {
        var existente = buscar(tenantId, documentoId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);

        Assinatura assinatura = jdbc.query("""
                SELECT a.id, a.documento_id, a.conteudo_assinado, a.hash_sha256,
                       a.tipo_assinatura, a.ambiente, d.solicitacao_id, d.filial_id
                FROM fiscal_documentos_assinaturas a
                JOIN fiscal_documentos d
                  ON d.tenant_id = a.tenant_id AND d.id = a.documento_id
                WHERE a.tenant_id = ? AND a.documento_id = ?
                """, (rs, n) -> new Assinatura(
                        rs.getObject("id", UUID.class), rs.getObject("documento_id", UUID.class),
                        rs.getString("conteudo_assinado"), rs.getString("hash_sha256"),
                        rs.getString("tipo_assinatura"), rs.getString("ambiente"),
                        rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("filial_id", UUID.class)),
                tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assinatura fiscal nao encontrada para o documento e tenant"));

        if (!"HOMOLOGACAO".equals(assinatura.ambiente())
                || !"SIMULADA".equals(assinatura.tipo())) {
            throw new IllegalArgumentException(
                    "Transmissao simulada exige assinatura simulada de HOMOLOGACAO");
        }

        var resposta = transmissor.transmitir(new FiscalTransmissaoPort.Comando(
                documentoId, assinatura.id(), assinatura.conteudo(), assinatura.hash()));
        UUID transmissaoId = UUID.nameUUIDFromBytes(
                (documentoId + ":transmissao:simulada").getBytes(StandardCharsets.UTF_8));
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_transmissoes
                    (id, tenant_id, solicitacao_id, documento_id, assinatura_id, ambiente,
                     provedor, status, codigo_resposta, mensagem_resposta, protocolo,
                     hash_requisicao, hash_resposta)
                VALUES (?, ?, ?, ?, ?, 'HOMOLOGACAO', ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (tenant_id, assinatura_id) DO NOTHING
                """, transmissaoId, tenantId, assinatura.solicitacaoId(), documentoId,
                assinatura.id(), resposta.provedor(), resposta.status(),
                resposta.codigoResposta(), resposta.mensagemResposta(), resposta.protocolo(),
                assinatura.hash(), resposta.hashResposta());

        Resultado resultado = inseridos == 1
                ? new Resultado(transmissaoId, assinatura.solicitacaoId(), documentoId,
                    assinatura.id(), resposta.status(), resposta.codigoResposta(),
                    resposta.mensagemResposta(), resposta.protocolo(), resposta.hashResposta(), false)
                : buscar(tenantId, documentoId).getFirst().comRepetida(true);
        if (inseridos == 1) {
            auditoria.registrar(tenantId, usuarioId, null, assinatura.filialId(),
                    "TRANSMITIR_FISCAL_HOMOLOGACAO_SIMULADA", "FISCAL_TRANSMISSAO",
                    transmissaoId, "documentoId=" + documentoId + ";assinaturaId="
                            + assinatura.id() + ";status=AUTORIZADO_SIMULADO;protocolo="
                            + resposta.protocolo());
        }
        return resultado;
    }

    private List<Resultado> buscar(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT id, solicitacao_id, documento_id, assinatura_id, status,
                       codigo_resposta, mensagem_resposta, protocolo, hash_resposta
                FROM fiscal_transmissoes
                WHERE tenant_id = ? AND documento_id = ?
                  AND ambiente = 'HOMOLOGACAO' AND provedor = 'SIMULADO'
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class), rs.getObject("solicitacao_id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("assinatura_id", UUID.class), rs.getString("status"),
                        rs.getString("codigo_resposta"), rs.getString("mensagem_resposta"),
                        rs.getString("protocolo"), rs.getString("hash_resposta"), false),
                tenantId, documentoId);
    }

    record Assinatura(UUID id, UUID documentoId, String conteudo, String hash,
                      String tipo, String ambiente, UUID solicitacaoId, UUID filialId) {}

    public record Resultado(UUID transmissaoId, UUID solicitacaoId, UUID documentoId,
                            UUID assinaturaId, String status, String codigoResposta,
                            String mensagemResposta, String protocolo, String hashResposta,
                            boolean repetida) {
        Resultado comRepetida(boolean valor) {
            return new Resultado(transmissaoId, solicitacaoId, documentoId, assinaturaId,
                    status, codigoResposta, mensagemResposta, protocolo, hashResposta, valor);
        }
    }
}
