package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaFinanceiraMovimento;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContaFinanceiraMovimentoResponse(
        UUID id,
        UUID contaFinanceiraId,
        String tipo,
        BigDecimal valor,
        String descricao,
        String origemTipo,
        UUID origemId,
        String origemReferencia,
        Instant ocorridoEm
) {
    public static ContaFinanceiraMovimentoResponse from(ContaFinanceiraMovimento movimento) {
        return new ContaFinanceiraMovimentoResponse(movimento.getId(), movimento.getContaFinanceiraId(),
                movimento.getTipo(), movimento.getValor(), movimento.getDescricao(),
                movimento.getOrigemTipo(), movimento.getOrigemId(), movimento.getOrigemReferencia(),
                movimento.getOcorridoEm());
    }
}
