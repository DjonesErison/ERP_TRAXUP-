package com.traxup.tplug.erp.inventario;

import java.util.UUID;

public record InventarioItemLeitura(
        String tipoItem,
        UUID itemId,
        String codigo,
        String descricao,
        String codigoBarra
) {}
