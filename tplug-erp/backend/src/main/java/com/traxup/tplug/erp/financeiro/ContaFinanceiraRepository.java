package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, UUID> {
    List<ContaFinanceira> findAllByTenantIdOrderByNomeAsc(UUID tenantId);
    Optional<ContaFinanceira> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndFilialIdAndNomeIgnoreCase(UUID tenantId, UUID filialId, String nome);
}
