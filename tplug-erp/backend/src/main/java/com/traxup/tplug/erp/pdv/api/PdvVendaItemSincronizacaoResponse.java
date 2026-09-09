package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.venda.PedidoVendaItem;
import java.math.BigDecimal;
import java.util.UUID;

public record PdvVendaItemSincronizacaoResponse(UUID itemId, UUID itemLocalId, UUID pedidoVendaId,
                                                 UUID produtoId, UUID gradeId, BigDecimal quantidade,
                                                 BigDecimal precoUnitario, BigDecimal totalItem, boolean repetido) {
    public static PdvVendaItemSincronizacaoResponse from(PedidoVendaItem item, boolean repetido) {
        return new PdvVendaItemSincronizacaoResponse(item.getId(), item.getPdvItemLocalId(), item.getPedidoVendaId(),
                item.getProdutoId(), item.getGradeId(), item.getQuantidade(), item.getPrecoUnitario(), item.getTotalItem(), repetido);
    }
}
