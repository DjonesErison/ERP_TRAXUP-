package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class LivroCaixaContabilidadeApplicationService {
    private static final int LIMITE_PADRAO = 500;
    private static final int LIMITE_MAXIMO = 1000;

    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public LivroCaixaContabilidadeApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado consultar(UUID tenantId, UUID usuarioId,
                               LocalDate inicio, LocalDate fim, Integer limite) {
        validarPeriodo(inicio, fim);
        int limiteEfetivo = validarLimite(limite);

        List<Lancamento> lancamentos = jdbc.query("""
                SELECT m.id, m.filial_id, m.conta_financeira_id,
                       c.nome AS conta_nome, c.tipo AS conta_tipo,
                       m.tipo AS movimento_tipo, m.valor, m.descricao,
                       m.origem_tipo, m.origem_id, m.ocorrido_em
                FROM contas_financeiras_movimentos m
                JOIN contas_financeiras c
                  ON c.tenant_id = m.tenant_id
                 AND c.id = m.conta_financeira_id
                WHERE m.tenant_id = ?
                  AND m.ocorrido_em >= ?
                  AND m.ocorrido_em < ?
                ORDER BY m.ocorrido_em, m.id
                LIMIT ?
                """, (rs, n) -> new Lancamento(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getObject("conta_financeira_id", UUID.class),
                        rs.getString("conta_nome"),
                        rs.getString("conta_tipo"),
                        rs.getString("movimento_tipo"),
                        rs.getBigDecimal("valor"),
                        rs.getString("descricao"),
                        rs.getString("origem_tipo"),
                        rs.getObject("origem_id", UUID.class),
                        rs.getTimestamp("ocorrido_em").toInstant()),
                tenantId,
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.plusDays(1).atStartOfDay()),
                limiteEfetivo + 1);

        if (lancamentos.size() > limiteEfetivo)
            throw new IllegalArgumentException(
                    "Livro Caixa excede o limite solicitado; reduza o periodo");

        Totais totais = calcularTotais(lancamentos);
        auditoria.registrar(tenantId, usuarioId, null, null,
                "CONSULTAR_LIVRO_CAIXA", "LIVRO_CAIXA", UUID.randomUUID(),
                "inicio=" + inicio + ";fim=" + fim
                        + ";lancamentos=" + lancamentos.size());

        return new Resultado(inicio, fim, lancamentos.size(),
                totais.entradas(), totais.saidas(), totais.saldo(),
                lancamentos);
    }

    static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null)
            throw new IllegalArgumentException(
                    "Data inicial e final sao obrigatorias");
        if (inicio.isAfter(fim))
            throw new IllegalArgumentException(
                    "Data inicial deve ser anterior ou igual a data final");
        if (ChronoUnit.DAYS.between(inicio, fim) > 365)
            throw new IllegalArgumentException(
                    "Livro Caixa permite no maximo 366 dias consecutivos");
    }

    static int validarLimite(Integer limite) {
        if (limite == null)
            return LIMITE_PADRAO;
        if (limite < 1 || limite > LIMITE_MAXIMO)
            throw new IllegalArgumentException(
                    "Limite deve estar entre 1 e 1000");
        return limite;
    }

    static Totais calcularTotais(List<Lancamento> lancamentos) {
        BigDecimal entradas = BigDecimal.ZERO;
        BigDecimal saidas = BigDecimal.ZERO;
        for (Lancamento lancamento : lancamentos) {
            if ("ENTRADA".equals(lancamento.tipo()))
                entradas = entradas.add(lancamento.valor());
            else if ("SAIDA".equals(lancamento.tipo()))
                saidas = saidas.add(lancamento.valor());
        }
        return new Totais(entradas, saidas, entradas.subtract(saidas));
    }

    record Totais(BigDecimal entradas, BigDecimal saidas, BigDecimal saldo) {}

    public record Lancamento(
            UUID id,
            UUID filialId,
            UUID contaFinanceiraId,
            String contaNome,
            String contaTipo,
            String tipo,
            BigDecimal valor,
            String descricao,
            String origemTipo,
            UUID origemId,
            Instant ocorridoEm) {}

    public record Resultado(
            LocalDate inicio,
            LocalDate fim,
            int totalLancamentos,
            BigDecimal totalEntradas,
            BigDecimal totalSaidas,
            BigDecimal saldoPeriodo,
            List<Lancamento> lancamentos) {}
}
