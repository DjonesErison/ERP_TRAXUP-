package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.contabilidade.EscopoFilialContabilidade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class InventarioContabilidadeApplicationService {
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public InventarioContabilidadeApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Resultado consultar(UUID tenantId, UUID usuarioId,
                               LocalDate inicio, LocalDate fim,
                               UUID filialId, Integer limite, int pagina) {
        validarPeriodo(inicio, fim);
        var escopo = escopoFilial.resolver(
                tenantId, usuarioId, filialId);
        int limiteEfetivo = validarLimite(limite);
        Timestamp instanteInicial = Timestamp.valueOf(inicio.atStartOfDay());
        Timestamp instanteFinal = Timestamp.valueOf(
                fim.plusDays(1).atStartOfDay());

        Long contagem = jdbc.queryForObject("""
                SELECT COUNT(*)
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
                """, Long.class, tenantId, instanteInicial, instanteFinal,
                filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId);
        long totalDisponivel = contagem == null ? 0 : contagem;
        long totalPaginas = totalPaginas(totalDisponivel, limiteEfetivo);
        validarPagina(pagina, totalPaginas);
        long deslocamento = (pagina - 1L) * limiteEfetivo;

        List<Posicao> posicoes = jdbc.query("""
                SELECT s.id, s.filial_id, s.descricao,
                       s.concluido_em, s.ajustado_em,
                       COUNT(c.id) AS total_itens,
                       COUNT(CASE WHEN c.divergencia <> 0 THEN 1 END)
                           AS itens_divergentes
                FROM inventario_sessoes s
                LEFT JOIN inventario_contagens c
                  ON c.tenant_id = s.tenant_id
                 AND c.inventario_id = s.id
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
                GROUP BY s.id, s.filial_id, s.descricao,
                         s.concluido_em, s.ajustado_em
                ORDER BY s.concluido_em DESC, s.id
                LIMIT ? OFFSET ?
                """, (rs, n) -> new Posicao(
                        rs.getObject("id", UUID.class),
                        rs.getObject("filial_id", UUID.class),
                        rs.getString("descricao"),
                        rs.getTimestamp("concluido_em").toInstant(),
                        rs.getTimestamp("ajustado_em") == null ? null
                                : rs.getTimestamp("ajustado_em").toInstant(),
                        rs.getLong("total_itens"),
                        rs.getLong("itens_divergentes")),
                tenantId, instanteInicial, instanteFinal,
                filialId, filialId,
                escopo.acessoTotal(), tenantId, usuarioId,
                limiteEfetivo, deslocamento);

        auditoria.registrar(tenantId, usuarioId, null, filialId,
                "CONSULTAR_INVENTARIO_CONTABIL", "INVENTARIO_SESSAO",
                UUID.randomUUID(), "inicio=" + inicio + ";fim=" + fim
                        + ";pagina=" + pagina
                        + ";totalPaginas=" + totalPaginas
                        + ";acessoTotal=" + escopo.acessoTotal()
                        + ";inventarios=" + posicoes.size());

        return new Resultado(inicio, fim, filialId, posicoes.size(),
                totalDisponivel, pagina, totalPaginas, posicoes);
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
                    "Inventario contabil permite no maximo 366 dias consecutivos");
    }

    static int validarLimite(Integer limite) {
        int valor = limite == null ? LIMITE_PADRAO : limite;
        if (valor < 1 || valor > LIMITE_MAXIMO)
            throw new IllegalArgumentException(
                    "Limite deve estar entre 1 e 500");
        return valor;
    }

    static long totalPaginas(long totalDisponivel, int limite) {
        if (totalDisponivel < 0 || limite < 1)
            throw new IllegalArgumentException(
                    "Total e limite da paginacao sao invalidos");
        return totalDisponivel == 0 ? 1
                : 1 + (totalDisponivel - 1) / limite;
    }

    static void validarPagina(int pagina, long totalPaginas) {
        if (pagina < 1 || pagina > totalPaginas)
            throw new IllegalArgumentException(
                    "Pagina deve estar entre 1 e " + totalPaginas);
    }

    public record Posicao(
            UUID inventarioId,
            UUID filialId,
            String descricao,
            Instant concluidoEm,
            Instant ajustadoEm,
            long totalItens,
            long itensDivergentes) {}

    public record Resultado(
            LocalDate inicio,
            LocalDate fim,
            UUID filialId,
            int totalNaPagina,
            long totalDisponivel,
            int pagina,
            long totalPaginas,
            List<Posicao> inventarios) {}
}
