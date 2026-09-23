package com.traxup.tplug.erp.trial;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TrialSaasRepository extends JpaRepository<TrialSaas, UUID> {
    boolean existsByDocumento(String documento);
    Optional<TrialSaas> findByDocumentoAndEmailIgnoreCase(String documento, String email);
    Optional<TrialSaas> findByIdempotencyKey(String idempotencyKey);
    @org.springframework.data.jpa.repository.Query("select t from TrialSaas t where t.tenant.id=:tenantId and lower(t.email)=lower(:email)")
    Optional<TrialSaas> findByTenantIdAndEmailIgnoreCase(@org.springframework.data.repository.query.Param("tenantId") UUID tenantId,@org.springframework.data.repository.query.Param("email") String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from TrialSaas t where t.administrador.id=:usuarioId")
    Optional<TrialSaas> lockByAdministradorId(@org.springframework.data.repository.query.Param("usuarioId") UUID usuarioId);
    @org.springframework.data.jpa.repository.Query("select count(t)>0 from TrialSaas t where t.tenant.id=:tenantId")
    boolean existsByTenantId(@org.springframework.data.repository.query.Param("tenantId") UUID tenantId);
}
