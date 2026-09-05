package com.traxup.tplug.erp.auth;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        long expiresInSeconds) {
}
