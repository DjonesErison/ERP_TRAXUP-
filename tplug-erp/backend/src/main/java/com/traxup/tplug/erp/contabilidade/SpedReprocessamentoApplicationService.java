package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SpedReprocessamentoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public SpedReprocessamentoApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado reprocessar(
            UUID tenantId, UUID usuarioId, UUID exportacaoId) {
        List<Resultado> atualizados = jdbc.query("""
                UPDATE contabilidade_sped_exportacoes
                SET status = 'PENDENTE',
                    tentativas_processamento = 0,
                    chave_objeto = NULL,
                    hash_sha256 = NULL,
                    versao_layout = NULL,
                    erro_codigo = NULL,
                    concluido_em = NULL,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ?
                  AND id = ?
                  AND status = 'FALHOU'
                RETURNING id, status
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class),
                        rs.getString("status")),
                tenantId, exportacaoId);
        if (atualizados.isEmpty())
            throw new RecursoNaoEncontradoException(
                    "Exportacao SPED com falha nao encontrada");

        auditoria.registrar(tenantId, usuarioId, null, null,
                "REPROCESSAR_SPED", "SPED_EXPORTACAO", exportacaoId,
                "status_anterior=FALHOU");
        return atualizados.getFirst();
    }

    public record Resultado(UUID id, String status) {}
}
