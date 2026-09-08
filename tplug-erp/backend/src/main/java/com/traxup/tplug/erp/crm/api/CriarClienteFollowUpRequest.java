package com.traxup.tplug.erp.crm.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CriarClienteFollowUpRequest(
        @NotNull UUID filialId,
        @NotNull UUID clienteId,
        @NotBlank @Size(max = 160) String assunto,
        @Size(max = 500) String observacao,
        @NotNull Instant agendadoPara
) {}
