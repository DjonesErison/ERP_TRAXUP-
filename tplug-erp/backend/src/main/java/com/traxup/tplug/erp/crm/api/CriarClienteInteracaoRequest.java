package com.traxup.tplug.erp.crm.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CriarClienteInteracaoRequest(
        @NotNull UUID filialId,
        @NotNull UUID clienteId,
        UUID followUpId,
        @NotBlank String canal,
        @NotBlank String resultado,
        @NotBlank String assunto,
        @NotNull Instant ocorridoEm
) {}
