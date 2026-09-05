package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.venda.api.PedidoVendaTotaisResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoVendaTotaisTest {
    @Test
    void calculaSubtotalDescontoETotalLiquido() {
        PedidoVendaItem primeiro = item("2.0000", "50.0000");
        primeiro.aplicarDesconto(new BigDecimal("10.0000"));
        PedidoVendaItem segundo = item("1.0000", "25.0000");
        segundo.aplicarDesconto(new BigDecimal("5.0000"));

        PedidoVendaTotaisResponse totais = PedidoVendaTotaisResponse.from(List.of(primeiro, segundo));

        assertEquals(0, totais.subtotalBruto().compareTo(new BigDecimal("125.0000")));
        assertEquals(0, totais.descontoTotal().compareTo(new BigDecimal("15.0000")));
        assertEquals(0, totais.totalLiquido().compareTo(new BigDecimal("110.0000")));
    }

    @Test
    void pedidoSemItensRetornaTotaisZero() {
        PedidoVendaTotaisResponse totais = PedidoVendaTotaisResponse.from(List.of());
        assertEquals(0, totais.subtotalBruto().compareTo(BigDecimal.ZERO));
        assertEquals(0, totais.descontoTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, totais.totalLiquido().compareTo(BigDecimal.ZERO));
    }

    private PedidoVendaItem item(String quantidade, String preco) {
        return new PedidoVendaItem(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                new BigDecimal(quantidade), new BigDecimal(preco));
    }
}
