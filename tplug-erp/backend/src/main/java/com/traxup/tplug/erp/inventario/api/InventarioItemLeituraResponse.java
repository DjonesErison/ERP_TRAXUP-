package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.inventario.InventarioItemLeitura;

import java.util.UUID;

public record InventarioItemLeituraResponse(
        String tipoItem,
        UUID itemId,
        String codigo,
        String descricao,
        String codigoBarra
) {
    public static InventarioItemLeituraResponse from(InventarioItemLeitura item) {
        return new InventarioItemLeituraResponse(item.tipoItem(), item.itemId(), item.codigo(), item.descricao(), item.codigoBarra());
    }
}
