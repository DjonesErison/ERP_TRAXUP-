package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceiraTentativa;
import java.time.Instant;
import java.util.UUID;

public record IntegracaoFinanceiraTentativaResponse(UUID id, UUID integracaoId, String provedor, String status,
        int quantidadeLancamentos, long duracaoMs, String erroCodigo, Instant iniciadoEm, Instant finalizadoEm) {
    public static IntegracaoFinanceiraTentativaResponse from(IntegracaoFinanceiraTentativa tentativa) {
        return new IntegracaoFinanceiraTentativaResponse(tentativa.getId(), tentativa.getIntegracaoId(),
                tentativa.getProvedor(), tentativa.getStatus(), tentativa.getQuantidadeLancamentos(),
                tentativa.getDuracaoMs(), tentativa.getErroCodigo(), tentativa.getIniciadoEm(), tentativa.getFinalizadoEm());
    }
}
