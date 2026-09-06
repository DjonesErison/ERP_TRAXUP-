package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceira;

import java.time.Instant;
import java.util.UUID;

public record IntegracaoFinanceiraResponse(
        UUID id,
        UUID contaFinanceiraId,
        UUID filialId,
        String provedor,
        String identificadorExterno,
        String checkpoint,
        Instant sincronizadoEm,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static IntegracaoFinanceiraResponse from(IntegracaoFinanceira integracao) {
        return new IntegracaoFinanceiraResponse(
                integracao.getId(), integracao.getContaFinanceiraId(), integracao.getFilialId(),
                integracao.getProvedor(), integracao.getIdentificadorExterno(), integracao.getCheckpoint(),
                integracao.getSincronizadoEm(), integracao.isAtivo(), integracao.getCriadoEm(),
                integracao.getAtualizadoEm());
    }
}
