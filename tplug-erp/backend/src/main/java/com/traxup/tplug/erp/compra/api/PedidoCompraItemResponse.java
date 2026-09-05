package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.compra.PedidoCompraItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PedidoCompraItemResponse(
        UUID id,
        UUID pedidoCompraId,
        String tipoItem,
        UUID itemId,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal totalItem,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PedidoCompraItemResponse from(PedidoCompraItem item) {
        return new PedidoCompraItemResponse(
                item.getId(),
                item.getPedidoCompraId(),
                item.getTipoItem(),
                item.getItemId(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getTotalItem(),
                item.getCriadoEm(),
                item.getAtualizadoEm()
        );
    }
}
