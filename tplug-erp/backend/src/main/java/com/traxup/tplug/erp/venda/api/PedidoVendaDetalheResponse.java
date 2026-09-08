package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaItem;
import com.traxup.tplug.erp.venda.PedidoVendaItemComboOpcao;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record PedidoVendaDetalheResponse(
        PedidoVendaResponse pedido,
        List<PedidoVendaItemResponse> itens,
        PedidoVendaTotaisResponse totais
) {
    public static PedidoVendaDetalheResponse from(PedidoVenda pedido, List<PedidoVendaItem> itens) {
        return from(pedido, itens, List.of());
    }

    public static PedidoVendaDetalheResponse from(PedidoVenda pedido, List<PedidoVendaItem> itens,
                                                  List<PedidoVendaItemComboOpcao> comboOpcoes) {
        Map<UUID, List<PedidoVendaItemComboOpcao>> opcoesPorItem = comboOpcoes.stream()
                .collect(Collectors.groupingBy(PedidoVendaItemComboOpcao::getPedidoVendaItemId));
        return new PedidoVendaDetalheResponse(
                PedidoVendaResponse.from(pedido),
                itens.stream()
                        .map(item -> PedidoVendaItemResponse.from(
                                item, opcoesPorItem.getOrDefault(item.getId(), List.of())))
                        .toList(),
                PedidoVendaTotaisResponse.from(itens));
    }
}
