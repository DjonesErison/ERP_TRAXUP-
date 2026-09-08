package com.traxup.tplug.erp.inventario;

public record InventarioDivergenciaItem(
        InventarioContagem contagem,
        String codigoItem,
        String descricaoItem
) {}
