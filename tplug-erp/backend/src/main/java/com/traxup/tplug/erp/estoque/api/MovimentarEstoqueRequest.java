package com.traxup.tplug.erp.estoque.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimentarEstoqueRequest(
        @NotNull UUID filialId,
        @NotBlank String tipoItem,
        @NotNull UUID itemId,
        @NotBlank String tipoMovimento,
        @NotNull @DecimalMin("0.0001") BigDecimal quantidade,
        @Size(max = 255) String motivo) {
}
