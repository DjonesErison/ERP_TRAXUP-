package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record SincronizarIntegracaoLancamentoRequest(
        @NotBlank @Size(max = 160) String referenciaExterna,
        @NotBlank @Size(max = 20) String tipo,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal valor,
        @NotBlank @Size(max = 200) String descricao,
        @NotNull Instant ocorridoEm
) {}
