package com.traxup.tplug.erp.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoCompraItemRepository extends JpaRepository<PedidoCompraItem, UUID> {
    List<PedidoCompraItem> findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(UUID tenantId, UUID pedidoCompraId);
}
