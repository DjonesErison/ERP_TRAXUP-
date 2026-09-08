package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PedidoVendaRecenteResponse(
        UUID id,
        UUID filialId,
        UUID clienteId,
        String numero,
        String status,
        BigDecimal totalLiquido,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PedidoVendaRecenteResponse from(PedidoVenda pedido, BigDecimal totalLiquido) {
        return new PedidoVendaRecenteResponse(
                pedido.getId(),
                pedido.getFilialId(),
                pedido.getClienteId(),
                pedido.getNumero(),
                pedido.getStatus(),
                totalLiquido == null ? BigDecimal.ZERO : totalLiquido,
                pedido.getCriadoEm(),
                pedido.getAtualizadoEm());
    }
}
