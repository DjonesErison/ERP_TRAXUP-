package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalHistoricoApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalHistoricoApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Historico buscar(UUID tenantId, UUID documentoId) {
        return jdbc.query("""
                SELECT d.id AS documento_id, d.solicitacao_id, d.filial_id,
                       d.pedido_venda_id, d.modelo, d.ambiente, d.serie, d.numero,
                       d.estruturado_em, s.status AS solicitacao_status,
                       s.criado_em AS solicitado_em, s.atualizado_em AS solicitacao_atualizado_em,
                       x.id AS xml_id, x.versao AS xml_versao, x.hash_sha256 AS xml_hash,
                       x.gerado_em AS xml_gerado_em,
                       a.id AS assinatura_id, a.tipo_assinatura, a.algoritmo,
                       a.hash_sha256 AS assinatura_hash, a.assinado_em,
                       t.id AS transmissao_id, t.provedor, t.status AS transmissao_status,
                       t.codigo_resposta, t.mensagem_resposta, t.protocolo,
                       t.transmitido_em,
                       p.id AS processado_id, p.tipo AS processado_tipo,
                       p.versao AS processado_versao, p.hash_sha256 AS processado_hash,
                       p.gerado_em AS processado_gerado_em
                FROM fiscal_documentos d
                JOIN fiscal_solicitacoes s
                  ON s.tenant_id = d.tenant_id AND s.id = d.solicitacao_id
                LEFT JOIN fiscal_documentos_xml x
                  ON x.tenant_id = d.tenant_id AND x.documento_id = d.id AND x.versao = '1.2'
                LEFT JOIN fiscal_documentos_assinaturas a
                  ON a.tenant_id = d.tenant_id AND a.documento_id = d.id
                LEFT JOIN fiscal_transmissoes t
                  ON t.tenant_id = d.tenant_id AND t.documento_id = d.id
                LEFT JOIN fiscal_documentos_processados p
                  ON p.tenant_id = d.tenant_id AND p.documento_id = d.id
                WHERE d.tenant_id = ? AND d.id = ?
                """, (rs, n) -> {
                    UUID xmlId = rs.getObject("xml_id", UUID.class);
                    UUID assinaturaId = rs.getObject("assinatura_id", UUID.class);
                    UUID transmissaoId = rs.getObject("transmissao_id", UUID.class);
                    UUID processadoId = rs.getObject("processado_id", UUID.class);
                    Integer serie = rs.getObject("serie", Integer.class);
                    Long numero = rs.getObject("numero", Long.class);
                    String etapa = determinarEtapa(numero, xmlId, assinaturaId,
                            transmissaoId, processadoId);
                    return new Historico(
                            rs.getObject("documento_id", UUID.class),
                            rs.getObject("solicitacao_id", UUID.class),
                            rs.getObject("filial_id", UUID.class),
                            rs.getObject("pedido_venda_id", UUID.class),
                            rs.getString("modelo"), rs.getString("ambiente"),
                            rs.getString("solicitacao_status"), etapa, serie, numero,
                            instante(rs.getTimestamp("solicitado_em")),
                            instante(rs.getTimestamp("solicitacao_atualizado_em")),
                            instante(rs.getTimestamp("estruturado_em")),
                            xmlId, rs.getString("xml_versao"), rs.getString("xml_hash"),
                            instante(rs.getTimestamp("xml_gerado_em")),
                            assinaturaId, rs.getString("tipo_assinatura"),
                            rs.getString("algoritmo"), rs.getString("assinatura_hash"),
                            instante(rs.getTimestamp("assinado_em")),
                            transmissaoId, rs.getString("provedor"),
                            rs.getString("transmissao_status"),
                            rs.getString("codigo_resposta"),
                            rs.getString("mensagem_resposta"), rs.getString("protocolo"),
                            instante(rs.getTimestamp("transmitido_em")),
                            processadoId, rs.getString("processado_tipo"),
                            rs.getString("processado_versao"),
                            rs.getString("processado_hash"),
                            instante(rs.getTimestamp("processado_gerado_em")));
                }, tenantId, documentoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Historico fiscal nao encontrado para o documento e tenant"));
    }

    static String determinarEtapa(Long numero, UUID xmlId, UUID assinaturaId,
                                  UUID transmissaoId, UUID processadoId) {
        if (processadoId != null) return "PROCESSADO_SIMULADO";
        if (transmissaoId != null) return "TRANSMITIDO_SIMULADO";
        if (assinaturaId != null) return "ASSINADO_SIMULADO";
        if (xmlId != null) return "XML_GERADO";
        if (numero != null) return "NUMERADO";
        return "ESTRUTURADO";
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    public record Historico(
            UUID documentoId, UUID solicitacaoId, UUID filialId, UUID pedidoVendaId,
            String modelo, String ambiente, String solicitacaoStatus, String etapaAtual,
            Integer serie, Long numero, Instant solicitadoEm, Instant solicitacaoAtualizadoEm,
            Instant estruturadoEm, UUID xmlId, String xmlVersao, String xmlHashSha256,
            Instant xmlGeradoEm, UUID assinaturaId, String assinaturaTipo,
            String assinaturaAlgoritmo, String assinaturaHashSha256, Instant assinadoEm,
            UUID transmissaoId, String transmissaoProvedor, String transmissaoStatus,
            String codigoResposta, String mensagemResposta, String protocolo,
            Instant transmitidoEm, UUID processadoId, String processadoTipo,
            String processadoVersao, String processadoHashSha256, Instant processadoGeradoEm) {}
}
