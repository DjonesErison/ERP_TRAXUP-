package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalTentativasHistoricoApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalTentativasHistoricoApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Historico buscar(UUID tenantId, UUID documentoId) {
        boolean documentoExiste = Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM fiscal_documentos WHERE tenant_id = ? AND id = ?
                )
                """, Boolean.class, tenantId, documentoId));
        if (!documentoExiste) throw new RecursoNaoEncontradoException(
                "Documento fiscal nao encontrado para o tenant");

        List<Tentativa> tentativas = jdbc.query("""
                SELECT te.id, te.numero, te.status, te.tentativa_anterior_id,
                       te.hash_documento_corrigido, te.criada_em, te.iniciada_em,
                       te.concluida_em,
                       r.id AS rejeicao_id, r.codigo AS rejeicao_codigo,
                       r.categoria AS rejeicao_categoria, r.status AS rejeicao_status,
                       c.id AS correcao_id, c.versao AS correcao_versao,
                       c.status AS correcao_status, c.hash_sha256 AS correcao_hash,
                       c.aplicada_em,
                       x.id AS xml_id, x.versao AS xml_versao,
                       x.hash_sha256 AS xml_hash, x.gerado_em AS xml_gerado_em,
                       a.id AS assinatura_id, a.tipo_assinatura,
                       a.hash_sha256 AS assinatura_hash, a.assinado_em,
                       tr.id AS transmissao_id, tr.provedor,
                       tr.status AS transmissao_status, tr.codigo_resposta,
                       tr.protocolo, tr.transmitido_em,
                       p.id AS processado_id, p.tipo AS processado_tipo,
                       p.hash_sha256 AS processado_hash,
                       p.gerado_em AS processado_gerado_em
                FROM fiscal_tentativas_emissao te
                JOIN fiscal_rejeicoes r
                  ON r.tenant_id = te.tenant_id AND r.id = te.rejeicao_id
                JOIN fiscal_rejeicao_correcoes c
                  ON c.tenant_id = te.tenant_id AND c.id = te.correcao_id
                LEFT JOIN fiscal_documentos_xml x
                  ON x.tenant_id = te.tenant_id AND x.tentativa_id = te.id
                LEFT JOIN fiscal_documentos_assinaturas a
                  ON a.tenant_id = te.tenant_id AND a.tentativa_id = te.id
                LEFT JOIN fiscal_transmissoes tr
                  ON tr.tenant_id = te.tenant_id AND tr.tentativa_id = te.id
                LEFT JOIN fiscal_documentos_processados p
                  ON p.tenant_id = te.tenant_id AND p.tentativa_id = te.id
                WHERE te.tenant_id = ? AND te.documento_id = ?
                ORDER BY te.numero
                """, (rs, n) -> new Tentativa(
                        rs.getObject("id", UUID.class), rs.getInt("numero"),
                        rs.getString("status"),
                        rs.getObject("tentativa_anterior_id", UUID.class),
                        rs.getString("hash_documento_corrigido"),
                        instante(rs.getTimestamp("criada_em")),
                        instante(rs.getTimestamp("iniciada_em")),
                        instante(rs.getTimestamp("concluida_em")),
                        new Rejeicao(rs.getObject("rejeicao_id", UUID.class),
                                rs.getString("rejeicao_codigo"),
                                rs.getString("rejeicao_categoria"),
                                rs.getString("rejeicao_status")),
                        new Correcao(rs.getObject("correcao_id", UUID.class),
                                rs.getInt("correcao_versao"),
                                rs.getString("correcao_status"),
                                rs.getString("correcao_hash"),
                                instante(rs.getTimestamp("aplicada_em"))),
                        artefato(rs.getObject("xml_id", UUID.class),
                                rs.getString("xml_versao"), rs.getString("xml_hash"),
                                instante(rs.getTimestamp("xml_gerado_em"))),
                        assinatura(rs.getObject("assinatura_id", UUID.class),
                                rs.getString("tipo_assinatura"),
                                rs.getString("assinatura_hash"),
                                instante(rs.getTimestamp("assinado_em"))),
                        transmissao(rs.getObject("transmissao_id", UUID.class),
                                rs.getString("provedor"),
                                rs.getString("transmissao_status"),
                                rs.getString("codigo_resposta"),
                                rs.getString("protocolo"),
                                instante(rs.getTimestamp("transmitido_em"))),
                        processado(rs.getObject("processado_id", UUID.class),
                                rs.getString("processado_tipo"),
                                rs.getString("processado_hash"),
                                instante(rs.getTimestamp("processado_gerado_em")))),
                tenantId, documentoId);
        return new Historico(documentoId, tentativas.size(), tentativas);
    }

    private static Xml artefato(UUID id, String versao, String hash, Instant geradoEm) {
        return id == null ? null : new Xml(id, versao, hash, geradoEm);
    }

    private static Assinatura assinatura(UUID id, String tipo, String hash, Instant em) {
        return id == null ? null : new Assinatura(id, tipo, hash, em);
    }

    private static Transmissao transmissao(UUID id, String provedor, String status,
                                           String codigo, String protocolo, Instant em) {
        return id == null ? null : new Transmissao(id, provedor, status, codigo, protocolo, em);
    }

    private static Processado processado(UUID id, String tipo, String hash, Instant em) {
        return id == null ? null : new Processado(id, tipo, hash, em);
    }

    private static Instant instante(Timestamp valor) {
        return valor == null ? null : valor.toInstant();
    }

    public record Historico(UUID documentoId, int totalTentativas,
                            List<Tentativa> tentativas) {}
    public record Tentativa(UUID id, int numero, String status,
                            UUID tentativaAnteriorId, String hashDocumentoCorrigido,
                            Instant criadaEm, Instant iniciadaEm, Instant concluidaEm,
                            Rejeicao rejeicao, Correcao correcao, Xml xml,
                            Assinatura assinatura, Transmissao transmissao,
                            Processado processado) {}
    public record Rejeicao(UUID id, String codigo, String categoria, String status) {}
    public record Correcao(UUID id, int versao, String status, String hashSha256,
                           Instant aplicadaEm) {}
    public record Xml(UUID id, String versao, String hashSha256, Instant geradoEm) {}
    public record Assinatura(UUID id, String tipo, String hashSha256, Instant assinadoEm) {}
    public record Transmissao(UUID id, String provedor, String status, String codigo,
                              String protocolo, Instant transmitidoEm) {}
    public record Processado(UUID id, String tipo, String hashSha256, Instant geradoEm) {}
}
