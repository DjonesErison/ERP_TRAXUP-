package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvVendaRascunhoApplicationService;

import java.util.UUID;

public record PdvVendaRascunhoResponse(
        UUID sincronizacaoId,
        UUID pedidoVendaId,
        String numeroPedido,
        String status,
        boolean repetida
) {
    public static PdvVendaRascunhoResponse from(PdvVendaRascunhoApplicationService.Resultado resultado) {
        return new PdvVendaRascunhoResponse(
                resultado.sincronizacao().getId(),
                resultado.pedidoVenda().getId(),
                resultado.pedidoVenda().getNumero(),
                resultado.pedidoVenda().getStatus(),
                resultado.repetida());
    }
}
