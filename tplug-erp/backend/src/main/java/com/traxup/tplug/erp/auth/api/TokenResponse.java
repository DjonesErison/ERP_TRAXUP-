package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.auth.AuthTokens;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken) {

    public static TokenResponse from(AuthTokens tokens) {
        return new TokenResponse(
                tokens.accessToken(),
                "Bearer",
                tokens.expiresInSeconds(),
                tokens.refreshToken());
    }
}
