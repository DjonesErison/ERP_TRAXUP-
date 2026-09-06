package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record ImportarConciliacaoLancamentoRequest(
        @NotBlank String origem,
        @NotBlank String referenciaExterna,
        @NotBlank String tipo,
        @NotNull @Positive BigDecimal valor,
        @NotBlank String descricao,
        @NotNull Instant ocorridoEm
) {}
