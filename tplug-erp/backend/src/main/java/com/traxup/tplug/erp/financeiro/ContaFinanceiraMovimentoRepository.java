package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFinanceiraMovimentoRepository extends JpaRepository<ContaFinanceiraMovimento, UUID> {
    List<ContaFinanceiraMovimento> findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(
            UUID tenantId, UUID contaFinanceiraId);
    Optional<ContaFinanceiraMovimento> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ContaFinanceiraMovimento> findAllByTenantIdAndContaFinanceiraIdAndFilialIdAndTipoAndValorAndOcorridoEmBetweenOrderByOcorridoEmAsc(
            UUID tenantId, UUID contaFinanceiraId, UUID filialId, String tipo, BigDecimal valor,
            Instant inicio, Instant fim);
}
