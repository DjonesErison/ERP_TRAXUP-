package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.shared.exception.AutenticacaoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantContext {

    public UUID tenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new AutenticacaoException("Token JWT autenticado obrigatorio");
        }

        String tenantId = jwtAuthenticationToken.getToken().getClaimAsString("tenant_id");
        if (tenantId == null || tenantId.isBlank()) {
            throw new AutenticacaoException("Token JWT sem identificacao de tenant");
        }

        try {
            return UUID.fromString(tenantId);
        } catch (IllegalArgumentException exception) {
            throw new AutenticacaoException("Token JWT com tenant invalido");
        }
    }
}
