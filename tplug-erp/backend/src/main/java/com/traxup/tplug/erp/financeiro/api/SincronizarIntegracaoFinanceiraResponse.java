package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceiraSincronizacaoService;

import java.util.List;

public record SincronizarIntegracaoFinanceiraResponse(
        IntegracaoFinanceiraResponse integracao,
        List<ConciliacaoLancamentoResponse> lancamentos
) {
    public static SincronizarIntegracaoFinanceiraResponse from(
            IntegracaoFinanceiraSincronizacaoService.ResultadoSincronizacao resultado) {
        return new SincronizarIntegracaoFinanceiraResponse(
                IntegracaoFinanceiraResponse.from(resultado.integracao()),
                resultado.lancamentos().stream().map(ConciliacaoLancamentoResponse::from).toList());
    }
}
