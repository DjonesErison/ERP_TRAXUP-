package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaItem;
import com.traxup.tplug.erp.venda.PedidoVendaItemComboOpcao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoVendaDetalheResponseTest {

    @Test
    void deveMontarDetalheComCabecalhoItensTotaisEOpcoesDoCombo() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        PedidoVenda pedido = new PedidoVenda(tenantId, filialId, null, "PV-001", null, UUID.randomUUID());

        PedidoVendaItem item1 = new PedidoVendaItem(tenantId, pedido.getId(), UUID.randomUUID(), null,
                new BigDecimal("2.0000"), new BigDecimal("10.0000"));
        item1.aplicarDesconto(new BigDecimal("1.0000"));
        PedidoVendaItem item2 = new PedidoVendaItem(tenantId, pedido.getId(), UUID.randomUUID(), null,
                new BigDecimal("1.0000"), new BigDecimal("5.0000"));
        UUID produtoOpcaoId = UUID.randomUUID();
        PedidoVendaItemComboOpcao opcao = new PedidoVendaItemComboOpcao(
                tenantId, item1.getId(), UUID.randomUUID(), UUID.randomUUID(), produtoOpcaoId,
                new BigDecimal("1.5000"), new BigDecimal("2.2500"));

        PedidoVendaDetalheResponse response = PedidoVendaDetalheResponse.from(
                pedido, List.of(item1, item2), List.of(opcao));

        assertEquals(pedido.getId(), response.pedido().id());
        assertEquals(2, response.itens().size());
        assertEquals(1, response.itens().get(0).comboOpcoes().size());
        assertEquals(produtoOpcaoId, response.itens().get(0).comboOpcoes().get(0).produtoId());
        assertEquals(new BigDecimal("1.5000"), response.itens().get(0).comboOpcoes().get(0).quantidade());
        assertEquals(new BigDecimal("2.2500"), response.itens().get(0).comboOpcoes().get(0).valorAdicional());
        assertEquals(List.of(), response.itens().get(1).comboOpcoes());
        assertEquals(new BigDecimal("25.00000000"), response.totais().subtotalBruto());
        assertEquals(new BigDecimal("1.0000"), response.totais().descontoTotal());
        assertEquals(new BigDecimal("24.00000000"), response.totais().totalLiquido());
    }
}
