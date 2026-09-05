package com.traxup.tplug.erp.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, UUID> {
    List<PedidoCompra> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
    Optional<PedidoCompra> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNumeroIgnoreCase(UUID tenantId, String numero);
}
