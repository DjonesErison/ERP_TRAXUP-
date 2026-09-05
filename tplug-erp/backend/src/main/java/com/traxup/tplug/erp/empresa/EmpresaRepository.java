package com.traxup.tplug.erp.empresa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    Optional<Empresa> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Empresa> findAllByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
