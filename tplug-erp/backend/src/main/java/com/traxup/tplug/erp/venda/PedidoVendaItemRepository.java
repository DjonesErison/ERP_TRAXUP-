package com.traxup.tplug.erp.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoVendaItemRepository extends JpaRepository<PedidoVendaItem, UUID> {
    List<PedidoVendaItem> findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(UUID tenantId, UUID pedidoVendaId);
}
