package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;

import java.time.Instant;
import java.util.UUID;

public record PedidoVendaResponse(
        UUID id,
        UUID filialId,
        UUID clienteId,
        String numero,
        String status,
        String observacao,
        UUID usuarioId,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PedidoVendaResponse from(PedidoVenda pedido) {
        return new PedidoVendaResponse(pedido.getId(), pedido.getFilialId(), pedido.getClienteId(), pedido.getNumero(),
                pedido.getStatus(), pedido.getObservacao(), pedido.getUsuarioId(), pedido.getCriadoEm(), pedido.getAtualizadoEm());
    }
}
