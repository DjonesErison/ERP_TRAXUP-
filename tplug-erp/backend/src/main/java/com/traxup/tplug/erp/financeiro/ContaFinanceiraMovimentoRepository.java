package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFinanceiraMovimentoRepository extends JpaRepository<ContaFinanceiraMovimento, UUID> {
    List<ContaFinanceiraMovimento> findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(
            UUID tenantId, UUID contaFinanceiraId);
    Optional<ContaFinanceiraMovimento> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT movimento FROM ContaFinanceiraMovimento movimento " +
            "WHERE movimento.id = :id AND movimento.tenantId = :tenantId")
    Optional<ContaFinanceiraMovimento> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                                    @Param("tenantId") UUID tenantId);

    @Query("""
            SELECT movimento FROM ContaFinanceiraMovimento movimento
            WHERE movimento.tenantId = :tenantId
              AND movimento.contaFinanceiraId = :contaFinanceiraId
              AND movimento.filialId = :filialId
              AND movimento.tipo = :tipo
              AND movimento.valor = :valor
              AND movimento.ocorridoEm BETWEEN :inicio AND :fim
              AND NOT EXISTS (
                  SELECT lancamento.id FROM ConciliacaoLancamento lancamento
                  WHERE lancamento.tenantId = movimento.tenantId
                    AND lancamento.movimentoId = movimento.id
              )
            ORDER BY movimento.ocorridoEm ASC
            """)
    List<ContaFinanceiraMovimento> findCandidatosDisponiveis(
            @Param("tenantId") UUID tenantId,
            @Param("contaFinanceiraId") UUID contaFinanceiraId,
            @Param("filialId") UUID filialId,
            @Param("tipo") String tipo,
            @Param("valor") BigDecimal valor,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);
}
