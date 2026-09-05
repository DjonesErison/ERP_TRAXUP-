package com.traxup.tplug.erp.empresa.api;

import com.traxup.tplug.erp.empresa.Empresa;

import java.time.Instant;
import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        UUID tenantId,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static EmpresaResponse from(UUID tenantId, Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                tenantId,
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getCnpj(),
                empresa.isAtivo(),
                empresa.getCriadoEm(),
                empresa.getAtualizadoEm());
    }
}
