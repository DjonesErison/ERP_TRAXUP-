package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.compra.PedidoCompraItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PedidoCompraItemResponse(
        UUID id,
        UUID pedidoCompraId,
        UUID produtoId,
        UUID gradeId,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal totalItem,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PedidoCompraItemResponse from(PedidoCompraItem item) {
        return new PedidoCompraItemResponse(
                item.getId(), item.getPedidoCompraId(), item.getProdutoId(), item.getGradeId(),
                item.getQuantidade(), item.getPrecoUnitario(), item.getTotalItem(),
                item.getCriadoEm(), item.getAtualizadoEm());
    }
}
