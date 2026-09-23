package com.traxup.tplug.erp.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    java.util.Optional<Tenant> findByCodigoEmpresa(String codigoEmpresa);
}
