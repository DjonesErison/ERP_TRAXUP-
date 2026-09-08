package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.crm.ClienteInativoProjection;

import java.time.Instant;
import java.util.UUID;

public record ClienteInativoResponse(
        UUID clienteId,
        String nomeRazaoSocial,
        String nomeFantasia,
        String email,
        String telefone,
        Instant ultimaCompraEm,
        long quantidadeCompras
) {
    public static ClienteInativoResponse from(ClienteInativoProjection projection) {
        return new ClienteInativoResponse(
                projection.getClienteId(),
                projection.getNomeRazaoSocial(),
                projection.getNomeFantasia(),
                projection.getEmail(),
                projection.getTelefone(),
                projection.getUltimaCompraEm(),
                projection.getQuantidadeCompras()
        );
    }
}
