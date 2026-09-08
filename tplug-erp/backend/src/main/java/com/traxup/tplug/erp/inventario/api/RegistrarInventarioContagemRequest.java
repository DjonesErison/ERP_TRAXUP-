package com.traxup.tplug.erp.inventario.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarInventarioContagemRequest(
        @NotBlank String tipoItem,
        @NotNull UUID itemId,
        @NotNull @DecimalMin(value = "0.0000", inclusive = true) BigDecimal quantidadeContada
) {}
