package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceiraPainelOperacionalService;

public record IntegracaoFinanceiraPainelOperacionalResponse(
        IntegracaoFinanceiraResponse integracao,
        IntegracaoFinanceiraSaudeResponse saude) {

    public static IntegracaoFinanceiraPainelOperacionalResponse from(
            IntegracaoFinanceiraPainelOperacionalService.ItemPainel item) {
        return new IntegracaoFinanceiraPainelOperacionalResponse(
                IntegracaoFinanceiraResponse.from(item.integracao()),
                IntegracaoFinanceiraSaudeResponse.from(item.saude()));
    }
}
