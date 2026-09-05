package com.traxup.tplug.erp.estoque.api;

import com.traxup.tplug.erp.estoque.EstoqueMovimentacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EstoqueMovimentacaoResponse(
        UUID id, UUID filialId, String tipoItem, UUID itemId, String tipoMovimento,
        BigDecimal quantidade, BigDecimal saldoAnterior, BigDecimal saldoPosterior,
        String motivo, UUID usuarioId, Instant criadoEm) {

    public static EstoqueMovimentacaoResponse from(EstoqueMovimentacao m) {
        return new EstoqueMovimentacaoResponse(m.getId(), m.getFilialId(), m.getTipoItem(), m.getItemId(),
                m.getTipoMovimento(), m.getQuantidade(), m.getSaldoAnterior(), m.getSaldoPosterior(),
                m.getMotivo(), m.getUsuarioId(), m.getCriadoEm());
    }
}
