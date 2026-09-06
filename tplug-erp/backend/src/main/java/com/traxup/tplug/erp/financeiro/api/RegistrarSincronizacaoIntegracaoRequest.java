package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record RegistrarSincronizacaoIntegracaoRequest(
        @Size(max = 500) String checkpoint,
        @NotNull Instant sincronizadoEm
) {}
