package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoVendaStatusTest {
    @Test
    void deveIniciarEmRascunho() {
        assertEquals("RASCUNHO", novoPedido().getStatus());
    }

    @Test
    void deveAbrirPedidoEmRascunho() {
        PedidoVenda pedido = novoPedido();
        pedido.abrir();
        assertEquals("ABERTO", pedido.getStatus());
    }

    @Test
    void naoDeveAbrirDuasVezes() {
        PedidoVenda pedido = novoPedido();
        pedido.abrir();
        assertThrows(IllegalArgumentException.class, pedido::abrir);
    }

    @Test
    void deveCancelarPedidoNaoFinalizado() {
        PedidoVenda pedido = novoPedido();
        pedido.cancelar();
        assertEquals("CANCELADO", pedido.getStatus());
        assertThrows(IllegalArgumentException.class, pedido::cancelar);
    }

    private PedidoVenda novoPedido() {
        return new PedidoVenda(UUID.randomUUID(), UUID.randomUUID(), null, "PV-001", null, UUID.randomUUID());
    }
}
