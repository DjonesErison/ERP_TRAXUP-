package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoVendaItemDescontoTest {

    @Test
    void aplicaDescontoERecalculaTotalLiquido() {
        PedidoVendaItem item = new PedidoVendaItem(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                new BigDecimal("2.0000"), new BigDecimal("50.0000"));

        item.aplicarDesconto(new BigDecimal("10.0000"));

        assertEquals(0, item.getDescontoValor().compareTo(new BigDecimal("10.0000")));
        assertEquals(0, item.getTotalItem().compareTo(new BigDecimal("90.0000")));
    }

    @Test
    void rejeitaDescontoMaiorQueTotalBruto() {
        PedidoVendaItem item = new PedidoVendaItem(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                BigDecimal.ONE, new BigDecimal("25.0000"));

        assertThrows(IllegalArgumentException.class,
                () -> item.aplicarDesconto(new BigDecimal("25.0001")));
    }

    @Test
    void aceitaDescontoIntegralComTotalZero() {
        PedidoVendaItem item = new PedidoVendaItem(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                BigDecimal.ONE, new BigDecimal("25.0000"));

        item.aplicarDesconto(new BigDecimal("25.0000"));

        assertEquals(0, item.getTotalItem().compareTo(BigDecimal.ZERO));
    }
}
