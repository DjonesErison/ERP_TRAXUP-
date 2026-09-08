package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ConciliacaoResumoProjection;

import java.math.BigDecimal;

public record ConciliacaoResumoResponse(
        long totalLancamentos,
        BigDecimal valorTotal,
        long pendentes,
        BigDecimal valorPendente,
        long conciliados,
        BigDecimal valorConciliado,
        long taxas,
        BigDecimal valorTaxas,
        long antecipacoes,
        BigDecimal valorAntecipacoes,
        long estornos,
        BigDecimal valorEstornos,
        long chargebacks,
        BigDecimal valorChargebacks) {

    public static ConciliacaoResumoResponse from(ConciliacaoResumoProjection resumo) {
        return new ConciliacaoResumoResponse(
                resumo.getTotalLancamentos(), resumo.getValorTotal(),
                resumo.getPendentes(), resumo.getValorPendente(),
                resumo.getConciliados(), resumo.getValorConciliado(),
                resumo.getTaxas(), resumo.getValorTaxas(),
                resumo.getAntecipacoes(), resumo.getValorAntecipacoes(),
                resumo.getEstornos(), resumo.getValorEstornos(),
                resumo.getChargebacks(), resumo.getValorChargebacks());
    }
}
