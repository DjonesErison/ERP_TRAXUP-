package com.traxup.tplug.erp.pdv.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PdvVendaItemSincronizacaoRequest(
        @NotNull UUID produtoId,
        UUID gradeId,
        @NotNull @Positive BigDecimal quantidade,
        @NotNull @DecimalMin(value = "0.00") BigDecimal precoUnitario,
        @DecimalMin(value = "0.00") BigDecimal descontoValor
) {}
