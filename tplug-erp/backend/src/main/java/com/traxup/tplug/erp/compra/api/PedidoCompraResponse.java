package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.compra.PedidoCompra;

import java.time.Instant;
import java.util.UUID;

public record PedidoCompraResponse(
        UUID id,
        UUID filialId,
        UUID fornecedorId,
        String numero,
        String status,
        String observacao,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static PedidoCompraResponse from(PedidoCompra pedido) {
        return new PedidoCompraResponse(
                pedido.getId(), pedido.getFilialId(), pedido.getFornecedorId(), pedido.getNumero(),
                pedido.getStatus(), pedido.getObservacao(), pedido.getCriadoEm(), pedido.getAtualizadoEm());
    }
}
