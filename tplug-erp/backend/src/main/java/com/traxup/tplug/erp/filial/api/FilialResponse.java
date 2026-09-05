package com.traxup.tplug.erp.filial.api;

import com.traxup.tplug.erp.filial.Filial;

import java.time.Instant;
import java.util.UUID;

public record FilialResponse(
        UUID id,
        UUID tenantId,
        UUID empresaId,
        String nome,
        String cnpj,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static FilialResponse from(UUID tenantId, Filial filial) {
        return new FilialResponse(
                filial.getId(),
                tenantId,
                filial.getEmpresa().getId(),
                filial.getNome(),
                filial.getCnpj(),
                filial.isAtivo(),
                filial.getCriadoEm(),
                filial.getAtualizadoEm());
    }
}
