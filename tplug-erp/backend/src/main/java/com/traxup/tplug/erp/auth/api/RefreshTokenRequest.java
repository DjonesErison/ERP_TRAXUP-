package com.traxup.tplug.erp.auth.api;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String refreshToken) {
}
