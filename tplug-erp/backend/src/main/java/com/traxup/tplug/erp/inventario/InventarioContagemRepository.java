package com.traxup.tplug.erp.inventario;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventarioContagemRepository extends JpaRepository<InventarioContagem, UUID> {
    Optional<InventarioContagem> findByTenantIdAndInventarioIdAndTipoItemAndItemId(
            UUID tenantId, UUID inventarioId, String tipoItem, UUID itemId);

    List<InventarioContagem> findAllByTenantIdAndInventarioIdOrderByTipoItemAscItemIdAsc(
            UUID tenantId, UUID inventarioId, Pageable pageable);

    List<InventarioContagem> findAllByTenantIdAndInventarioIdAndDivergenciaNotOrderByTipoItemAscItemIdAsc(
            UUID tenantId, UUID inventarioId, BigDecimal divergencia, Pageable pageable);

    List<InventarioContagem> findAllByTenantIdAndInventarioIdOrderByTipoItemAscItemIdAsc(
            UUID tenantId, UUID inventarioId);

    boolean existsByTenantIdAndInventarioId(UUID tenantId, UUID inventarioId);
}
