package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVendaItem;

import java.math.BigDecimal;
import java.util.List;

public record PedidoVendaTotaisResponse(
        BigDecimal subtotalBruto,
        BigDecimal descontoTotal,
        BigDecimal totalLiquido
) {
    public static PedidoVendaTotaisResponse from(List<PedidoVendaItem> itens) {
        BigDecimal subtotal = itens.stream()
                .map(item -> item.getQuantidade().multiply(item.getPrecoUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descontos = itens.stream()
                .map(PedidoVendaItem::getDescontoValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal liquido = itens.stream()
                .map(PedidoVendaItem::getTotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PedidoVendaTotaisResponse(subtotal, descontos, liquido);
    }
}
