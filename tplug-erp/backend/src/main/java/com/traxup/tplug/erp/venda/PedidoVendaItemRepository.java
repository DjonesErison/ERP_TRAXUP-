package com.traxup.tplug.erp.venda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoVendaItemRepository extends JpaRepository<PedidoVendaItem, UUID> {
    List<PedidoVendaItem> findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(UUID tenantId, UUID pedidoVendaId);
    Optional<PedidoVendaItem> findByIdAndTenantIdAndPedidoVendaId(UUID id, UUID tenantId, UUID pedidoVendaId);

    @Query("""
            select i.pedidoVendaId as pedidoVendaId, sum(i.totalItem) as totalLiquido
            from PedidoVendaItem i
            where i.tenantId = :tenantId
              and i.pedidoVendaId in :pedidoIds
            group by i.pedidoVendaId
            """)
    List<TotalPedido> somarTotaisPorPedidos(@Param("tenantId") UUID tenantId,
                                             @Param("pedidoIds") List<UUID> pedidoIds);

    interface TotalPedido {
        UUID getPedidoVendaId();
        BigDecimal getTotalLiquido();
    }
}
