package com.traxup.tplug.erp.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Usuario> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    List<Usuario> findAllByTenantId(UUID tenantId);

    boolean existsByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
}
