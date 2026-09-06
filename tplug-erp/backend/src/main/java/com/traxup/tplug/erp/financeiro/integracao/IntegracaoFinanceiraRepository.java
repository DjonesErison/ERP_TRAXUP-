package com.traxup.tplug.erp.financeiro.integracao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegracaoFinanceiraRepository extends JpaRepository<IntegracaoFinanceira, UUID> {
    List<IntegracaoFinanceira> findAllByTenantIdAndContaFinanceiraIdOrderByProvedorAsc(UUID tenantId, UUID contaFinanceiraId);
    Optional<IntegracaoFinanceira> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndContaFinanceiraIdAndProvedor(UUID tenantId, UUID contaFinanceiraId, String provedor);
}
