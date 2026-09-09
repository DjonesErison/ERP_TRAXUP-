package com.traxup.tplug.erp.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FiscalPerfilFilialRepository extends JpaRepository<FiscalPerfilFilial, UUID> {

    Optional<FiscalPerfilFilial> findByTenantIdAndFilialId(UUID tenantId, UUID filialId);

    Optional<FiscalPerfilFilial> findByTenantIdAndFilialIdAndAtivoTrue(UUID tenantId, UUID filialId);
}
