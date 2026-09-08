package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVendaItem;
import com.traxup.tplug.erp.venda.PedidoVendaItemComboOpcao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PedidoVendaItemResponse(
        UUID id,
        UUID pedidoVendaId,
        UUID produtoId,
        UUID gradeId,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal descontoValor,
        BigDecimal totalItem,
        Instant criadoEm,
        Instant atualizadoEm,
        List<PedidoVendaItemComboOpcaoResponse> comboOpcoes
) {
    public static PedidoVendaItemResponse from(PedidoVendaItem item) {
        return from(item, List.of());
    }

    public static PedidoVendaItemResponse from(PedidoVendaItem item, List<PedidoVendaItemComboOpcao> comboOpcoes) {
        return new PedidoVendaItemResponse(
                item.getId(), item.getPedidoVendaId(), item.getProdutoId(), item.getGradeId(),
                item.getQuantidade(), item.getPrecoUnitario(), item.getDescontoValor(), item.getTotalItem(),
                item.getCriadoEm(), item.getAtualizadoEm(),
                comboOpcoes.stream().map(PedidoVendaItemComboOpcaoResponse::from).toList());
    }
}
