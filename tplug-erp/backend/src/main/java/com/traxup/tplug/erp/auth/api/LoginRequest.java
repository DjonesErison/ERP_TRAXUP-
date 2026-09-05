package com.traxup.tplug.erp.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record LoginRequest(
        @NotNull UUID tenantId,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 72) String senha) {
}
