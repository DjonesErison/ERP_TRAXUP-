package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoVendaDetalheResponseTest {

    @Test
    void deveMontarDetalheComCabecalhoItensETotais() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        PedidoVenda pedido = new PedidoVenda(tenantId, filialId, null, "PV-001", null, UUID.randomUUID());

        PedidoVendaItem item1 = new PedidoVendaItem(tenantId, pedido.getId(), UUID.randomUUID(), null,
                new BigDecimal("2.0000"), new BigDecimal("10.0000"));
        item1.aplicarDesconto(new BigDecimal("1.0000"));
        PedidoVendaItem item2 = new PedidoVendaItem(tenantId, pedido.getId(), UUID.randomUUID(), null,
                new BigDecimal("1.0000"), new BigDecimal("5.0000"));

        PedidoVendaDetalheResponse response = PedidoVendaDetalheResponse.from(pedido, List.of(item1, item2));

        assertEquals(pedido.getId(), response.pedido().id());
        assertEquals(2, response.itens().size());
        assertEquals(new BigDecimal("25.00000000"), response.totais().subtotalBruto());
        assertEquals(new BigDecimal("1.0000"), response.totais().descontoTotal());
        assertEquals(new BigDecimal("24.00000000"), response.totais().totalLiquido());
    }
}
