package com.traxup.tplug.erp.compra;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoCompraStatusTest {
    @Test
    void deveAbrirEChegarARecebido() {
        PedidoCompra pedido = novoPedido();
        pedido.abrir();
        assertEquals("ABERTO", pedido.getStatus());
        pedido.marcarRecebido();
        assertEquals("RECEBIDO", pedido.getStatus());
    }

    @Test
    void naoDeveAbrirDuasVezes() {
        PedidoCompra pedido = novoPedido();
        pedido.abrir();
        assertThrows(IllegalArgumentException.class, pedido::abrir);
    }

    @Test
    void naoDeveReceberPedidoEmRascunho() {
        assertThrows(IllegalArgumentException.class, novoPedido()::marcarRecebido);
    }

    @Test
    void naoDeveCancelarPedidoRecebido() {
        PedidoCompra pedido = novoPedido();
        pedido.abrir();
        pedido.marcarRecebido();
        assertThrows(IllegalArgumentException.class, pedido::cancelar);
    }

    private PedidoCompra novoPedido() {
        return new PedidoCompra(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "PC-001", null);
    }
}
