package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvVendaFechamentoApplicationService;

import java.util.UUID;

public record PdvVendaFechamentoResponse(
        UUID pedidoVendaId,
        String status,
        UUID formaPagamentoId,
        UUID condicaoPagamentoId,
        boolean repetida
) {
    public static PdvVendaFechamentoResponse from(PdvVendaFechamentoApplicationService.Resultado resultado) {
        var pedido = resultado.pedidoVenda();
        return new PdvVendaFechamentoResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getFormaPagamentoId(),
                pedido.getCondicaoPagamentoId(),
                resultado.repetida());
    }
}
