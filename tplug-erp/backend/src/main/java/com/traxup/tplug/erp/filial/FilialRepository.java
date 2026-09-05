package com.traxup.tplug.erp.filial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilialRepository extends JpaRepository<Filial, UUID> {

    Optional<Filial> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Filial> findAllByTenantId(UUID tenantId);

    List<Filial> findAllByTenantIdAndEmpresaId(UUID tenantId, UUID empresaId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
