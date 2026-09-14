package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SpedCancelamentoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public SpedCancelamentoApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional
    public Resultado cancelar(
            UUID tenantId, UUID usuarioId, UUID exportacaoId) {
        var escopo = escopoFilial.resolver(tenantId, usuarioId, null);
        List<Resultado> cancelados = jdbc.query("""
                UPDATE contabilidade_sped_exportacoes
                SET status = 'CANCELADO',
                    chave_objeto = NULL,
                    hash_sha256 = NULL,
                    versao_layout = NULL,
                    erro_codigo = NULL,
                    concluido_em = NULL,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE tenant_id = ?
                  AND id = ?
                  AND (CAST(? AS BOOLEAN) = TRUE OR filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                  AND status IN ('PENDENTE', 'FALHOU')
                RETURNING id, filial_id, status
                """, (rs, n) -> new Resultado(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("status")),
                tenantId, exportacaoId,
                escopo.acessoTotal(), tenantId, usuarioId);
        if (cancelados.isEmpty())
            throw new RecursoNaoEncontradoException(
                    "Exportacao SPED cancelavel nao encontrada");

        auditoria.registrar(tenantId, usuarioId, null,
                cancelados.getFirst().filialId(),
                "CANCELAR_SPED", "SPED_EXPORTACAO", exportacaoId,
                "status=CANCELADO");
        return cancelados.getFirst();
    }

    public record Resultado(UUID id, UUID filialId, String status) {}
}
