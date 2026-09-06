package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, UUID> {
    List<ContaFinanceira> findAllByTenantIdOrderByNomeAsc(UUID tenantId);
    Optional<ContaFinanceira> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT conta FROM ContaFinanceira conta WHERE conta.id = :id AND conta.tenantId = :tenantId")
    Optional<ContaFinanceira> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                           @Param("tenantId") UUID tenantId);

    boolean existsByTenantIdAndFilialIdAndNomeIgnoreCase(UUID tenantId, UUID filialId, String nome);
}
