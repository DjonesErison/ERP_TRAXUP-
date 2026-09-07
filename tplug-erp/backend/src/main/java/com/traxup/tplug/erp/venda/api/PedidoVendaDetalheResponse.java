package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaItem;

import java.util.List;

public record PedidoVendaDetalheResponse(
        PedidoVendaResponse pedido,
        List<PedidoVendaItemResponse> itens,
        PedidoVendaTotaisResponse totais
) {
    public static PedidoVendaDetalheResponse from(PedidoVenda pedido, List<PedidoVendaItem> itens) {
        return new PedidoVendaDetalheResponse(
                PedidoVendaResponse.from(pedido),
                itens.stream().map(PedidoVendaItemResponse::from).toList(),
                PedidoVendaTotaisResponse.from(itens));
    }
}
