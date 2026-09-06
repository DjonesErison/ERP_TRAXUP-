package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceiraObservabilidadeService;
import java.time.Instant;
import java.util.UUID;

public record IntegracaoFinanceiraSaudeResponse(
        UUID integracaoId,
        String status,
        Instant ultimaTentativaEm,
        Instant ultimoSucessoEm,
        int falhasConsecutivas,
        int tentativasConsideradas) {

    public static IntegracaoFinanceiraSaudeResponse from(IntegracaoFinanceiraObservabilidadeService.ResumoSaude resumo) {
        return new IntegracaoFinanceiraSaudeResponse(
                resumo.integracaoId(), resumo.status(), resumo.ultimaTentativaEm(), resumo.ultimoSucessoEm(),
                resumo.falhasConsecutivas(), resumo.tentativasConsideradas());
    }
}
