package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegistrarRecebimentoContaRequest(
        @NotNull
        @DecimalMin(value = "0.0001", inclusive = true)
        BigDecimal valor
) {}
