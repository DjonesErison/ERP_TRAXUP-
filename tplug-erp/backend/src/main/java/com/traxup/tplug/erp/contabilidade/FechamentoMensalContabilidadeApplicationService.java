package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class FechamentoMensalContabilidadeApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public FechamentoMensalContabilidadeApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Resumo consultar(UUID tenantId, UUID usuarioId,
                            YearMonth competencia, UUID filialId) {
        if (competencia == null)
            throw new IllegalArgumentException("Competencia e obrigatoria");
        var escopo = escopoFilial.resolver(
                tenantId, usuarioId, filialId);
        LocalDate inicio = competencia.atDay(1);
        LocalDate fimExclusivo = competencia.plusMonths(1).atDay(1);
        Timestamp instanteInicial = Timestamp.valueOf(inicio.atStartOfDay());
        Timestamp instanteFinal = Timestamp.valueOf(
                fimExclusivo.atStartOfDay());

        XmlResumo xml = jdbc.queryForObject("""
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE a.status = 'ARQUIVADO')
                           AS arquivados,
                       COUNT(*) FILTER (
                           WHERE a.status IN ('PENDENTE', 'ARQUIVANDO')
                       ) AS pendentes,
                       COUNT(*) FILTER (WHERE a.status = 'FALHOU')
                           AS falhas
                FROM fiscal_arquivos a
                JOIN fiscal_documentos d
                  ON d.tenant_id = a.tenant_id AND d.id = a.documento_id
                JOIN fiscal_documentos_processados p
                  ON p.tenant_id = a.tenant_id AND p.id = a.processado_id
                JOIN fiscal_transmissoes t
                  ON t.tenant_id = p.tenant_id AND t.id = p.transmissao_id
                WHERE a.tenant_id = ?
                  AND t.transmitido_em >= ?
                  AND t.transmitido_em < ?
                  AND (CAST(? AS UUID) IS NULL OR d.filial_id = ?)
                  AND (CAST(? AS BOOLEAN) = TRUE OR d.filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                """, (rs, n) -> new XmlResumo(
                        rs.getLong("total"),
                        rs.getLong("arquivados"),
                        rs.getLong("pendentes"),
                        rs.getLong("falhas")),
                tenantId, instanteInicial, instanteFinal,
                filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId);

        SpedResumo sped = jdbc.queryForObject("""
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE status = 'CONCLUIDO')
                           AS concluidos,
                       COUNT(*) FILTER (
                           WHERE status IN ('PENDENTE', 'PROCESSANDO')
                       ) AS pendentes,
                       COUNT(*) FILTER (WHERE status = 'FALHOU')
                           AS falhas,
                       COUNT(*) FILTER (WHERE status = 'CANCELADO')
                           AS cancelados
                FROM contabilidade_sped_exportacoes
                WHERE tenant_id = ? AND competencia = ?
                """, (rs, n) -> new SpedResumo(
                        rs.getLong("total"),
                        rs.getLong("concluidos"),
                        rs.getLong("pendentes"),
                        rs.getLong("falhas"),
                        rs.getLong("cancelados")),
                tenantId, Date.valueOf(inicio));

        LivroCaixaResumo livroCaixa = jdbc.queryForObject("""
                SELECT COUNT(*) AS lancamentos,
                       COALESCE(SUM(CASE WHEN m.tipo = 'ENTRADA'
                           THEN m.valor ELSE 0 END), 0) AS entradas,
                       COALESCE(SUM(CASE WHEN m.tipo = 'SAIDA'
                           THEN m.valor ELSE 0 END), 0) AS saidas
                FROM contas_financeiras_movimentos m
                JOIN contas_financeiras c
                  ON c.tenant_id = m.tenant_id
                 AND c.id = m.conta_financeira_id
                WHERE m.tenant_id = ?
                  AND m.ocorrido_em >= ?
                  AND m.ocorrido_em < ?
                  AND (CAST(? AS UUID) IS NULL OR m.filial_id = ?)
                  AND (CAST(? AS BOOLEAN) = TRUE OR m.filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                """, (rs, n) -> new LivroCaixaResumo(
                        rs.getLong("lancamentos"),
                        rs.getBigDecimal("entradas"),
                        rs.getBigDecimal("saidas")),
                tenantId, instanteInicial, instanteFinal,
                filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId);

        InventarioResumo inventario = jdbc.queryForObject("""
                SELECT COUNT(*) AS concluidos,
                       COUNT(*) FILTER (WHERE s.ajustado_em IS NOT NULL)
                           AS ajustados,
                       COUNT(*) FILTER (WHERE EXISTS (
                           SELECT 1
                           FROM inventario_contagens c
                           WHERE c.tenant_id = s.tenant_id
                             AND c.inventario_id = s.id
                             AND c.divergencia <> 0
                       )) AS com_divergencias
                FROM inventario_sessoes s
                WHERE s.tenant_id = ?
                  AND s.status = 'CONCLUIDO'
                  AND s.concluido_em >= ?
                  AND s.concluido_em < ?
                  AND (CAST(? AS UUID) IS NULL OR s.filial_id = ?)
                  AND (CAST(? AS BOOLEAN) = TRUE OR s.filial_id IN (
                      SELECT uf.filial_id
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = ? AND uf.usuario_id = ?
                  ))
                """, (rs, n) -> new InventarioResumo(
                        rs.getLong("concluidos"),
                        rs.getLong("ajustados"),
                        rs.getLong("com_divergencias")),
                tenantId, instanteInicial, instanteFinal,
                filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId);

        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "CONSULTAR_FECHAMENTO_MENSAL", "FECHAMENTO_CONTABIL",
                UUID.randomUUID(), "competencia=" + competencia
                        + ";filialId=" + filialId
                        + ";acessoTotal=" + escopo.acessoTotal());
        return new Resumo(competencia, filialId,
                xml, sped, livroCaixa, inventario);
    }

    public record XmlResumo(
            long total, long arquivados, long pendentes, long falhas) {}

    public record SpedResumo(
            long total, long concluidos, long pendentes,
            long falhas, long cancelados) {}

    public record LivroCaixaResumo(
            long lancamentos, BigDecimal entradas, BigDecimal saidas) {
        public BigDecimal saldo() {
            return entradas.subtract(saidas);
        }
    }

    public record InventarioResumo(
            long concluidos, long ajustados, long comDivergencias) {}

    public record Resumo(
            YearMonth competencia,
            UUID filialId,
            XmlResumo xml,
            SpedResumo sped,
            LivroCaixaResumo livroCaixa,
            InventarioResumo inventario) {}
}
