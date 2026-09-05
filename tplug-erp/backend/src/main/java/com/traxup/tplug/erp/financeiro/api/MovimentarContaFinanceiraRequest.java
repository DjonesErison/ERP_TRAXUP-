package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MovimentarContaFinanceiraRequest(
        @NotBlank String tipo,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal valor,
        @NotBlank String descricao
) {}
