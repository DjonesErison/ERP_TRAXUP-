package com.traxup.tplug.erp.estoque.api;

import com.traxup.tplug.erp.estoque.EstoqueSaldo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EstoqueSaldoResponse(
        UUID id,
        UUID filialId,
        String tipoItem,
        UUID itemId,
        BigDecimal quantidade,
        Instant atualizadoEm) {

    public static EstoqueSaldoResponse from(EstoqueSaldo saldo) {
        return new EstoqueSaldoResponse(
                saldo.getId(),
                saldo.getFilialId(),
                saldo.getTipoItem(),
                saldo.getItemId(),
                saldo.getQuantidade(),
                saldo.getAtualizadoEm());
    }
}
