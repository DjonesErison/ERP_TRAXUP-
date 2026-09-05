package com.traxup.tplug.erp.usuario.api;

import com.traxup.tplug.erp.usuario.Usuario;

import java.time.Instant;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        UUID tenantId,
        String nome,
        String email,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static UsuarioResponse from(UUID tenantId, Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                tenantId,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm());
    }
}
