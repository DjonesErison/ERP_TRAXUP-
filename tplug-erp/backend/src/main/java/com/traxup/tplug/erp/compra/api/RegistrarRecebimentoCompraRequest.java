package com.traxup.tplug.erp.compra.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RegistrarRecebimentoCompraRequest(
        @Size(max = 60) String documento,
        @Size(max = 500) String observacao,
        @Valid List<ItemRecebido> itens
) {
    public Map<UUID, BigDecimal> quantidadesPorItem() {
        if (itens == null) return Map.of();
        Map<UUID, BigDecimal> resultado = new LinkedHashMap<>();
        for (ItemRecebido item : itens) {
            if (resultado.put(item.pedidoItemId(), item.quantidadeRecebida()) != null) {
                throw new IllegalArgumentException("Item de pedido repetido no recebimento");
            }
        }
        return resultado;
    }

    public record ItemRecebido(
            @NotNull UUID pedidoItemId,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal quantidadeRecebida
    ) {}
}
