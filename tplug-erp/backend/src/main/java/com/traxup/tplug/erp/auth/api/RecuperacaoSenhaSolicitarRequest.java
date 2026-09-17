package com.traxup.tplug.erp.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RecuperacaoSenhaSolicitarRequest(
        @NotNull UUID tenantId,
        @NotBlank @Email String email
) {
}
