package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record SincronizarIntegracaoFinanceiraRequest(
        @Size(max = 500) String checkpoint,
        @NotNull Instant sincronizadoEm,
        @NotEmpty @Size(max = 500) List<@Valid SincronizarIntegracaoLancamentoRequest> lancamentos
) {}
