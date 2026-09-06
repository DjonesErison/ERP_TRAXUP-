package com.traxup.tplug.erp.financeiro.integracao;

import java.time.Instant;
import java.util.List;

public interface IntegracaoFinanceiraAdapter {
    String provedor();

    Resultado buscar(IntegracaoFinanceira integracao);

    record Resultado(List<IntegracaoFinanceiraSincronizacaoService.LancamentoExterno> lancamentos,
                     String checkpoint,
                     Instant sincronizadoEm) {
        public Resultado {
            lancamentos = lancamentos == null ? List.of() : List.copyOf(lancamentos);
            if (lancamentos.size() > 500) {
                throw new IllegalArgumentException("Adapter excedeu o limite de 500 lancamentos por sincronizacao");
            }
            if (sincronizadoEm == null) {
                throw new IllegalArgumentException("Adapter deve informar a data/hora da sincronizacao");
            }
        }
    }
}
