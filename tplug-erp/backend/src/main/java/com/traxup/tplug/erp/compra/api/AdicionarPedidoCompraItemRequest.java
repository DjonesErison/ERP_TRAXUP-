package com.traxup.tplug.erp.compra.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdicionarPedidoCompraItemRequest(
        @NotNull UUID produtoId,
        UUID gradeId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal quantidade,
        @NotNull @DecimalMin(value = "0.0") BigDecimal precoUnitario
) {}
