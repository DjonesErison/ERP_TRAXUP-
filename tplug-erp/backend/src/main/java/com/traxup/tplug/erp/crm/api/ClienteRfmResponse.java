package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.crm.ClienteRfmProjection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClienteRfmResponse(
        UUID clienteId,
        String nomeRazaoSocial,
        String nomeFantasia,
        Instant ultimaCompraEm,
        Long quantidadeCompras,
        BigDecimal valorTotal
) {
    public static ClienteRfmResponse from(ClienteRfmProjection projection) {
        return new ClienteRfmResponse(
                projection.getClienteId(),
                projection.getNomeRazaoSocial(),
                projection.getNomeFantasia(),
                projection.getUltimaCompraEm(),
                projection.getQuantidadeCompras(),
                projection.getValorTotal()
        );
    }
}
