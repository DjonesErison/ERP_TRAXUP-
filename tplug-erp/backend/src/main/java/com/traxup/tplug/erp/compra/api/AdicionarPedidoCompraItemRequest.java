package com.traxup.tplug.erp.compra.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdicionarPedidoCompraItemRequest(
        @NotBlank String tipoItem,
        @NotNull UUID itemId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal quantidade,
        @NotNull @DecimalMin(value = "0.0") BigDecimal precoUnitario
) {}
