package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CriarContaReceberRequest(
        @NotNull UUID filialId,
        @NotNull UUID clienteId,
        @NotBlank @Size(max = 60) String numeroDocumento,
        @NotBlank @Size(max = 200) String descricao,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valorOriginal,
        @NotNull LocalDate vencimento
) {}
