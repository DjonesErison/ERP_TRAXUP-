package com.traxup.tplug.erp.financeiro.integracao;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegracaoFinanceiraRepository extends JpaRepository<IntegracaoFinanceira, UUID> {
    List<IntegracaoFinanceira> findAllByTenantIdAndContaFinanceiraIdOrderByProvedorAsc(UUID tenantId, UUID contaFinanceiraId);
    Optional<IntegracaoFinanceira> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IntegracaoFinanceira i where i.id = :id and i.tenantId = :tenantId")
    Optional<IntegracaoFinanceira> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                                @Param("tenantId") UUID tenantId);

    boolean existsByTenantIdAndContaFinanceiraIdAndProvedor(UUID tenantId, UUID contaFinanceiraId, String provedor);
}
