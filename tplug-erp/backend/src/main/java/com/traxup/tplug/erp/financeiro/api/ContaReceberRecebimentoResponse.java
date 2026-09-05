package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaReceberRecebimento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContaReceberRecebimentoResponse(
        UUID id,
        UUID contaReceberId,
        UUID filialId,
        BigDecimal valor,
        UUID usuarioId,
        Instant recebidoEm
) {
    public static ContaReceberRecebimentoResponse from(ContaReceberRecebimento recebimento) {
        return new ContaReceberRecebimentoResponse(
                recebimento.getId(), recebimento.getContaReceberId(), recebimento.getFilialId(),
                recebimento.getValor(), recebimento.getUsuarioId(), recebimento.getRecebidoEm());
    }
}
