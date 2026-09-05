package com.traxup.tplug.erp.venda.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AplicarDescontoPedidoVendaItemRequest(
        @NotNull @DecimalMin(value = "0.0000") BigDecimal descontoValor
) {}
