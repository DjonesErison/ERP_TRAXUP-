package com.traxup.tplug.erp.perfil;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
    List<Perfil> findAllByTenantId(UUID tenantId);
    Optional<Perfil> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNomeIgnoreCase(UUID tenantId, String nome);
}
