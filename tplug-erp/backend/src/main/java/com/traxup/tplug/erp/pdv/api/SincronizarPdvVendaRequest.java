package com.traxup.tplug.erp.pdv.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record SincronizarPdvVendaRequest(
        @NotNull UUID terminalId,
        @NotNull UUID operacaoLocalId,
        @NotNull @Positive Long numeroLocal,
        @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{64}$") String checksum,
        @NotNull Instant ocorridoEm
) {}
