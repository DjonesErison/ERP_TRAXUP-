package com.traxup.tplug.erp.venda.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdicionarPedidoVendaItemRequest(
        @NotNull UUID produtoId,
        UUID gradeId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal quantidade,
        @NotNull @DecimalMin(value = "0.0") BigDecimal precoUnitario
) {}
