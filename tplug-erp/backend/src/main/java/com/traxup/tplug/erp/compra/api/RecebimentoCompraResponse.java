package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.compra.RecebimentoCompra;

import java.time.Instant;
import java.util.UUID;

public record RecebimentoCompraResponse(UUID id, UUID pedidoCompraId, UUID filialId, UUID fornecedorId,
                                        String status, String documento, String observacao,
                                        UUID usuarioId, Instant recebidoEm, Instant criadoEm) {
    public static RecebimentoCompraResponse from(RecebimentoCompra recebimento) {
        return new RecebimentoCompraResponse(recebimento.getId(), recebimento.getPedidoCompraId(),
                recebimento.getFilialId(), recebimento.getFornecedorId(), recebimento.getStatus(),
                recebimento.getDocumento(), recebimento.getObservacao(), recebimento.getUsuarioId(),
                recebimento.getRecebidoEm(), recebimento.getCriadoEm());
    }
}
