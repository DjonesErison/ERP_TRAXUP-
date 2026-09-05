package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CriarContaPagarRequest(
        @NotNull UUID filialId,
        @NotNull UUID fornecedorId,
        @NotBlank @Size(max = 60) String numeroDocumento,
        @NotBlank @Size(max = 200) String descricao,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal valorOriginal,
        @NotNull LocalDate vencimento
) {}
