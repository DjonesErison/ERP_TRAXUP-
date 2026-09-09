package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.fiscal.FiscalSolicitacao;

import java.time.Instant;
import java.util.UUID;

public record FiscalSolicitacaoResponse(
        UUID id,
        UUID filialId,
        UUID pedidoVendaId,
        String modelo,
        String ambiente,
        String status,
        Instant criadoEm,
        Instant atualizadoEm,
        boolean repetida
) {
    public static FiscalSolicitacaoResponse from(FiscalSolicitacao solicitacao, boolean repetida) {
        return new FiscalSolicitacaoResponse(
                solicitacao.getId(), solicitacao.getFilialId(), solicitacao.getPedidoVendaId(),
                solicitacao.getModelo(), solicitacao.getAmbiente(), solicitacao.getStatus(),
                solicitacao.getCriadoEm(), solicitacao.getAtualizadoEm(), repetida);
    }
}
