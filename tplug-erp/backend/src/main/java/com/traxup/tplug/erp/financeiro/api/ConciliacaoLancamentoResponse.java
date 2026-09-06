package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ConciliacaoLancamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ConciliacaoLancamentoResponse(
        UUID id,
        UUID contaFinanceiraId,
        String origem,
        String referenciaExterna,
        String tipo,
        BigDecimal valor,
        String descricao,
        String natureza,
        Instant ocorridoEm,
        String status,
        UUID movimentoId,
        Instant conciliadoEm,
        Instant criadoEm
) {
    public static ConciliacaoLancamentoResponse from(ConciliacaoLancamento lancamento) {
        return new ConciliacaoLancamentoResponse(
                lancamento.getId(), lancamento.getContaFinanceiraId(), lancamento.getOrigem(),
                lancamento.getReferenciaExterna(), lancamento.getTipo(), lancamento.getValor(),
                lancamento.getDescricao(), lancamento.getNatureza(), lancamento.getOcorridoEm(), lancamento.getStatus(),
                lancamento.getMovimentoId(), lancamento.getConciliadoEm(), lancamento.getCriadoEm());
    }
}
