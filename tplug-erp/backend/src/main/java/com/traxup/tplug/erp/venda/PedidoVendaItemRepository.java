package com.traxup.tplug.erp.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoVendaItemRepository extends JpaRepository<PedidoVendaItem, UUID> {
    List<PedidoVendaItem> findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(UUID tenantId, UUID pedidoVendaId);
    Optional<PedidoVendaItem> findByIdAndTenantIdAndPedidoVendaId(UUID id, UUID tenantId, UUID pedidoVendaId);
}
