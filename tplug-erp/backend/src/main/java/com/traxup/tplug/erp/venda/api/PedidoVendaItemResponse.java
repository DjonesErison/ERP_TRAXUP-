package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVendaItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PedidoVendaItemResponse(
        UUID id,
        UUID pedidoVendaId,
        UUID produtoId,
        UUID gradeId,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal totalItem,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PedidoVendaItemResponse from(PedidoVendaItem item) {
        return new PedidoVendaItemResponse(
                item.getId(), item.getPedidoVendaId(), item.getProdutoId(), item.getGradeId(),
                item.getQuantidade(), item.getPrecoUnitario(), item.getTotalItem(),
                item.getCriadoEm(), item.getAtualizadoEm());
    }
}
