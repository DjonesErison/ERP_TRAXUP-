package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConciliacaoLancamentoRepository extends JpaRepository<ConciliacaoLancamento, UUID> {
    List<ConciliacaoLancamento> findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDescIdAsc(UUID tenantId, UUID contaFinanceiraId);

    @Query("SELECT lancamento FROM ConciliacaoLancamento lancamento " +
            "WHERE lancamento.tenantId = :tenantId " +
            "AND lancamento.contaFinanceiraId = :contaId " +
            "AND (:origem IS NULL OR lancamento.origem = :origem) " +
            "AND (:natureza IS NULL OR lancamento.natureza = :natureza) " +
            "AND (:status IS NULL OR lancamento.status = :status) " +
            "AND (:inicio IS NULL OR lancamento.ocorridoEm >= :inicio) " +
            "AND (:fim IS NULL OR lancamento.ocorridoEm <= :fim) " +
            "ORDER BY lancamento.ocorridoEm DESC, lancamento.id ASC")
    List<ConciliacaoLancamento> filtrar(@Param("tenantId") UUID tenantId,
                                        @Param("contaId") UUID contaId,
                                        @Param("origem") String origem,
                                        @Param("natureza") String natureza,
                                        @Param("status") String status,
                                        @Param("inicio") Instant inicio,
                                        @Param("fim") Instant fim);

    @Query(value = """
            SELECT
                COUNT(*) AS "totalLancamentos",
                COALESCE(SUM(valor), 0) AS "valorTotal",
                COUNT(*) FILTER (WHERE status = 'PENDENTE') AS "pendentes",
                COALESCE(SUM(valor) FILTER (WHERE status = 'PENDENTE'), 0) AS "valorPendente",
                COUNT(*) FILTER (WHERE status = 'CONCILIADO') AS "conciliados",
                COALESCE(SUM(valor) FILTER (WHERE status = 'CONCILIADO'), 0) AS "valorConciliado",
                COUNT(*) FILTER (WHERE natureza = 'TAXA') AS "taxas",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'TAXA'), 0) AS "valorTaxas",
                COUNT(*) FILTER (WHERE natureza = 'ANTECIPACAO') AS "antecipacoes",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'ANTECIPACAO'), 0) AS "valorAntecipacoes",
                COUNT(*) FILTER (WHERE natureza = 'ESTORNO') AS "estornos",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'ESTORNO'), 0) AS "valorEstornos",
                COUNT(*) FILTER (WHERE natureza = 'CHARGEBACK') AS "chargebacks",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'CHARGEBACK'), 0) AS "valorChargebacks"
            FROM conciliacao_lancamentos
            WHERE tenant_id = :tenantId
              AND conta_financeira_id = :contaId
            """, nativeQuery = true)
    ConciliacaoResumoProjection resumir(@Param("tenantId") UUID tenantId,
                                         @Param("contaId") UUID contaId);

    @Query(value = """
            SELECT
                COUNT(*) AS "totalLancamentos",
                COALESCE(SUM(valor), 0) AS "valorTotal",
                COUNT(*) FILTER (WHERE status = 'PENDENTE') AS "pendentes",
                COALESCE(SUM(valor) FILTER (WHERE status = 'PENDENTE'), 0) AS "valorPendente",
                COUNT(*) FILTER (WHERE status = 'CONCILIADO') AS "conciliados",
                COALESCE(SUM(valor) FILTER (WHERE status = 'CONCILIADO'), 0) AS "valorConciliado",
                COUNT(*) FILTER (WHERE natureza = 'TAXA') AS "taxas",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'TAXA'), 0) AS "valorTaxas",
                COUNT(*) FILTER (WHERE natureza = 'ANTECIPACAO') AS "antecipacoes",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'ANTECIPACAO'), 0) AS "valorAntecipacoes",
                COUNT(*) FILTER (WHERE natureza = 'ESTORNO') AS "estornos",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'ESTORNO'), 0) AS "valorEstornos",
                COUNT(*) FILTER (WHERE natureza = 'CHARGEBACK') AS "chargebacks",
                COALESCE(SUM(valor) FILTER (WHERE natureza = 'CHARGEBACK'), 0) AS "valorChargebacks"
            FROM conciliacao_lancamentos
            WHERE tenant_id = :tenantId
              AND conta_financeira_id = :contaId
              AND (CAST(:origem AS varchar) IS NULL OR origem = :origem)
              AND (CAST(:natureza AS varchar) IS NULL OR natureza = :natureza)
              AND (CAST(:status AS varchar) IS NULL OR status = :status)
              AND (CAST(:inicio AS timestamptz) IS NULL OR ocorrido_em >= :inicio)
              AND (CAST(:fim AS timestamptz) IS NULL OR ocorrido_em <= :fim)
            """, nativeQuery = true)
    ConciliacaoResumoProjection resumirFiltrado(@Param("tenantId") UUID tenantId,
                                                 @Param("contaId") UUID contaId,
                                                 @Param("origem") String origem,
                                                 @Param("natureza") String natureza,
                                                 @Param("status") String status,
                                                 @Param("inicio") Instant inicio,
                                                 @Param("fim") Instant fim);

    Optional<ConciliacaoLancamento> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<ConciliacaoLancamento> findByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
            UUID tenantId, UUID contaFinanceiraId, String origem, String referenciaExterna);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lancamento FROM ConciliacaoLancamento lancamento " +
            "WHERE lancamento.id = :id AND lancamento.tenantId = :tenantId")
    Optional<ConciliacaoLancamento> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                                 @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT id FROM contas_financeiras " +
            "WHERE tenant_id = :tenantId AND id = :contaId FOR UPDATE", nativeQuery = true)
    Optional<UUID> bloquearContaParaImportacao(@Param("tenantId") UUID tenantId,
                                                @Param("contaId") UUID contaId);

    boolean existsByTenantIdAndMovimentoIdAndIdNot(UUID tenantId, UUID movimentoId, UUID id);
}
