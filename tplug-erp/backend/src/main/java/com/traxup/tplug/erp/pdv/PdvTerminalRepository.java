package com.traxup.tplug.erp.pdv;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvTerminalRepository extends JpaRepository<PdvTerminal, UUID> {
    Optional<PdvTerminal> findByIdAndTenantId(UUID id, UUID tenantId);
    List<PdvTerminal> findAllByTenantIdOrderByNomeAsc(UUID tenantId);
    List<PdvTerminal> findAllByTenantIdAndFilialIdOrderByNomeAsc(UUID tenantId, UUID filialId);
    boolean existsByTenantIdAndCodigo(UUID tenantId, String codigo);
    boolean existsByTenantIdAndFilialIdAndSerie(UUID tenantId, UUID filialId, Integer serie);
}
