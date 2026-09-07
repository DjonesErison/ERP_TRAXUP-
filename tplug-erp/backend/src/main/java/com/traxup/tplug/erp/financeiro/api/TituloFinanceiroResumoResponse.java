package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.TituloFinanceiroResumo;

import java.math.BigDecimal;

public record TituloFinanceiroResumoResponse(
        long quantidade,
        BigDecimal valorOriginalTotal,
        BigDecimal valorLiquidadoTotal,
        BigDecimal saldoAtivoTotal,
        long abertos,
        long parciais,
        long liquidados,
        long cancelados) {

    public static TituloFinanceiroResumoResponse from(TituloFinanceiroResumo resumo) {
        return new TituloFinanceiroResumoResponse(
                resumo.quantidade(),
                resumo.valorOriginalTotal(),
                resumo.valorLiquidadoTotal(),
                resumo.saldoAtivoTotal(),
                resumo.abertos(),
                resumo.parciais(),
                resumo.liquidados(),
                resumo.cancelados());
    }
}
