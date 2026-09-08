package com.traxup.tplug.erp.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoVendaItemComboOpcaoRepository extends JpaRepository<PedidoVendaItemComboOpcao, UUID> {
    List<PedidoVendaItemComboOpcao> findAllByTenantIdAndPedidoVendaItemIdOrderByGrupoIdAscOpcaoIdAsc(UUID tenantId, UUID pedidoVendaItemId);
    List<PedidoVendaItemComboOpcao> findAllByTenantIdAndPedidoVendaItemIdInOrderByPedidoVendaItemIdAscGrupoIdAscOpcaoIdAsc(
            UUID tenantId, List<UUID> pedidoVendaItemIds);
    long deleteByTenantIdAndPedidoVendaItemId(UUID tenantId, UUID pedidoVendaItemId);
}
