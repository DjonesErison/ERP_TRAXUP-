package com.traxup.tplug.erp.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TrialSaasRepository extends JpaRepository<TrialSaas, UUID> {
    Optional<TrialSaas> findByIdempotencyKey(String idempotencyKey);
    boolean existsByTenantId(UUID tenantId);
}
