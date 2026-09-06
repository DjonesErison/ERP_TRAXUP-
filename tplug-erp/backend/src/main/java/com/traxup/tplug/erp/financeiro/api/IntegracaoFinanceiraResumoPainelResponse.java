package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceiraPainelOperacionalService;

public record IntegracaoFinanceiraResumoPainelResponse(
        long total,
        long saudaveis,
        long atencao,
        long semExecucao) {

    public static IntegracaoFinanceiraResumoPainelResponse from(
            IntegracaoFinanceiraPainelOperacionalService.ResumoPainel resumo) {
        return new IntegracaoFinanceiraResumoPainelResponse(
                resumo.total(), resumo.saudaveis(), resumo.atencao(), resumo.semExecucao());
    }
}
