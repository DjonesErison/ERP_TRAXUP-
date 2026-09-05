package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CriarContaFinanceiraRequest(
        @NotNull UUID filialId,
        @NotBlank String nome,
        @NotBlank String tipo
) {}
