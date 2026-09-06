package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConciliacaoLancamentoRepository extends JpaRepository<ConciliacaoLancamento, UUID> {
    List<ConciliacaoLancamento> findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDesc(UUID tenantId, UUID contaFinanceiraId);
    Optional<ConciliacaoLancamento> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndContaFinanceiraIdAndOrigemAndReferenciaExterna(
            UUID tenantId, UUID contaFinanceiraId, String origem, String referenciaExterna);
}
