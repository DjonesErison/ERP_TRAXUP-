package com.traxup.tplug.erp.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TrialSaasRepository extends JpaRepository<TrialSaas, UUID> {
    Optional<TrialSaas> findByIdempotencyKey(String idempotencyKey);
    Optional<TrialSaas> findByTenantIdAndEmailIgnoreCase(UUID tenantId,String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from TrialSaas t where t.administrador.id=:usuarioId")
    Optional<TrialSaas> lockByAdministradorId(@org.springframework.data.repository.query.Param("usuarioId") UUID usuarioId);
    boolean existsByTenantId(UUID tenantId);
}
