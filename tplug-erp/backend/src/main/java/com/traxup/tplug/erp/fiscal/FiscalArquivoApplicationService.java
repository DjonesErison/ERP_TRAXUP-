package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalArquivoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalArquivoApplicationService(JdbcTemplate jdbc,
                                           AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado preparar(UUID tenantId, UUID usuarioId, UUID processadoId) {
        var existente = buscarInterno(tenantId, processadoId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetido(true);

        Origem origem = jdbc.query("""
                SELECT p.documento_id, p.tentativa_id, p.hash_sha256,
                       p.tipo, d.filial_id
                FROM fiscal_documentos_processados p
                JOIN fiscal_documentos d
                  ON d.tenant_id = p.tenant_id AND d.id = p.documento_id
                WHERE p.tenant_id = ? AND p.id = ?
                """, (rs, n) -> new Origem(
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getString("hash_sha256"), rs.getString("tipo"),
                        rs.getObject("filial_id", UUID.class)),
                tenantId, processadoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "XML processado nao encontrado para o tenant"));

        if (!"PROCESSADO_SIMULADO".equals(origem.tipo()))
            throw new IllegalArgumentException(
                    "Tipo de XML processado ainda nao suportado para arquivamento");

        UUID id = UUID.nameUUIDFromBytes((processadoId + ":arquivo")
                .getBytes(StandardCharsets.UTF_8));
        String chave = chave(tenantId, origem.documentoId(), processadoId);
        int inseridos = jdbc.update("""
                INSERT INTO fiscal_arquivos
                  (id, tenant_id, documento_id, tentativa_id, processado_id,
                   tipo, chave_objeto, hash_sha256, retencao_ate)
                VALUES (?, ?, ?, ?, ?, 'XML_PROCESSADO_SIMULADO', ?, ?,
                        CURRENT_DATE + INTERVAL '5 years')
                ON CONFLICT (tenant_id, processado_id) DO NOTHING
                """, id, tenantId, origem.documentoId(), origem.tentativaId(),
                processadoId, chave, origem.hash());

        Resultado resultado = inseridos == 1
                ? buscarInterno(tenantId, processadoId).getFirst()
                : buscarInterno(tenantId, processadoId).stream().findFirst()
                    .map(r -> r.comRepetido(true)).orElseThrow();
        if (inseridos == 1) {
            auditoria.registrar(tenantId, usuarioId, null, origem.filialId(),
                    "PREPARAR_ARQUIVAMENTO_FISCAL", "FISCAL_ARQUIVO", id,
                    "documentoId=" + origem.documentoId() + ";processadoId="
                            + processadoId + ";status=PENDENTE;retencaoAte="
                            + resultado.retencaoAte());
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Resultado buscar(UUID tenantId, UUID processadoId) {
        return buscarInterno(tenantId, processadoId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Arquivo fiscal nao encontrado para o tenant"));
    }

    private List<Resultado> buscarInterno(UUID tenantId, UUID processadoId) {
        return jdbc.query("""
                SELECT id, documento_id, tentativa_id, processado_id, tipo,
                       chave_objeto, hash_sha256, status, tentativas_envio,
                       retencao_ate
                FROM fiscal_arquivos
                WHERE tenant_id = ? AND processado_id = ?
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class),
                        rs.getObject("documento_id", UUID.class),
                        rs.getObject("tentativa_id", UUID.class),
                        rs.getObject("processado_id", UUID.class),
                        rs.getString("tipo"), rs.getString("chave_objeto"),
                        rs.getString("hash_sha256"), rs.getString("status"),
                        rs.getInt("tentativas_envio"),
                        rs.getObject("retencao_ate", LocalDate.class), false),
                tenantId, processadoId);
    }

    static String chave(UUID tenantId, UUID documentoId, UUID processadoId) {
        return "tenants/" + tenantId + "/fiscal/documentos/" + documentoId
                + "/processados/" + processadoId + ".xml";
    }

    record Origem(UUID documentoId, UUID tentativaId, String hash,
                  String tipo, UUID filialId) {}

    public record Resultado(UUID arquivoId, UUID documentoId, UUID tentativaId,
                            UUID processadoId, String tipo, String chaveObjeto,
                            String hashSha256, String status, int tentativasEnvio,
                            LocalDate retencaoAte, boolean repetido) {
        Resultado comRepetido(boolean valor) {
            return new Resultado(arquivoId, documentoId, tentativaId, processadoId,
                    tipo, chaveObjeto, hashSha256, status, tentativasEnvio,
                    retencaoAte, valor);
        }
    }
}
