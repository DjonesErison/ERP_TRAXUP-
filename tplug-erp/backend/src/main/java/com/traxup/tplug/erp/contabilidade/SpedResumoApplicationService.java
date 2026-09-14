package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class SpedResumoApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public SpedResumoApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional
    public Resumo resumir(
            UUID tenantId, UUID usuarioId, UUID filialId,
            String tipo, YearMonth competenciaInicio,
            YearMonth competenciaFim) {
        String tipoNormalizado = tipo == null || tipo.isBlank()
                ? null
                : SpedExportacaoApplicationService.normalizarTipo(tipo);
        SpedExportacaoApplicationService.validarIntervalo(
                competenciaInicio, competenciaFim);
        Date inicio = competenciaInicio == null ? null
                : Date.valueOf(competenciaInicio.atDay(1));
        Date fim = competenciaFim == null ? null
                : Date.valueOf(competenciaFim.atDay(1));
        var escopo = escopoFilial.resolver(
                tenantId, usuarioId, filialId);

        Resumo resumo = jdbc.queryForObject("""
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (
                           WHERE status = 'PENDENTE'
                       ) AS pendentes,
                       COUNT(*) FILTER (
                           WHERE status = 'PROCESSANDO'
                       ) AS processando,
                       COUNT(*) FILTER (
                           WHERE status = 'CONCLUIDO'
                       ) AS concluidos,
                       COUNT(*) FILTER (
                           WHERE status = 'FALHOU'
                       ) AS falhas,
                       COUNT(*) FILTER (
                           WHERE status = 'CANCELADO'
                       ) AS cancelados
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ?
                  AND (CAST(? AS UUID) IS NULL OR filial_id = ?)
                  AND (CAST(? AS BOOLEAN) = TRUE OR filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                  AND (CAST(? AS VARCHAR) IS NULL OR tipo = ?)
                  AND (CAST(? AS DATE) IS NULL OR competencia >= ?)
                  AND (CAST(? AS DATE) IS NULL OR competencia <= ?)
                """, (rs, n) -> new Resumo(
                        rs.getLong("total"),
                        rs.getLong("pendentes"),
                        rs.getLong("processando"),
                        rs.getLong("concluidos"),
                        rs.getLong("falhas"),
                        rs.getLong("cancelados")),
                tenantId, filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId,
                tipoNormalizado, tipoNormalizado,
                inicio, inicio, fim, fim);

        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "RESUMIR_EXPORTACOES_SPED", "SPED_EXPORTACAO",
                UUID.randomUUID(), "tipo=" + tipoNormalizado
                        + ";inicio=" + competenciaInicio
                        + ";fim=" + competenciaFim
                        + ";filialId=" + filialId);
        return resumo;
    }

    public record Resumo(
            long total,
            long pendentes,
            long processando,
            long concluidos,
            long falhas,
            long cancelados) {}
}
