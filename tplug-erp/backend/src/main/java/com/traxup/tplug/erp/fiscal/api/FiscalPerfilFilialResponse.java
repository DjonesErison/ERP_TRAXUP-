package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.fiscal.FiscalPerfilFilial;

import java.time.Instant;
import java.util.UUID;

public record FiscalPerfilFilialResponse(
        UUID id,
        UUID filialId,
        String regimeTributario,
        short crt,
        String ambiente,
        int serieNfe,
        int serieNfce,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static FiscalPerfilFilialResponse from(FiscalPerfilFilial perfil) {
        return new FiscalPerfilFilialResponse(
                perfil.getId(),
                perfil.getFilialId(),
                perfil.getRegimeTributario(),
                perfil.getCrt(),
                perfil.getAmbiente(),
                perfil.getSerieNfe(),
                perfil.getSerieNfce(),
                perfil.isAtivo(),
                perfil.getCriadoEm(),
                perfil.getAtualizadoEm());
    }
}
