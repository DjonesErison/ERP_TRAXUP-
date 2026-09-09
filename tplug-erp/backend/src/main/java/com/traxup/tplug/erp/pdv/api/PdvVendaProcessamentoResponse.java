package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvVendaProcessamentoApplicationService;

import java.time.Instant;
import java.util.UUID;

public record PdvVendaProcessamentoResponse(
        UUID sincronizacaoId,
        UUID terminalId,
        UUID operacaoLocalId,
        Integer serie,
        Long numeroLocal,
        UUID pedidoVendaId,
        String pedidoNumero,
        String pedidoStatus,
        Instant processadoEm,
        boolean repetida
) {
    public static PdvVendaProcessamentoResponse from(PdvVendaProcessamentoApplicationService.Resultado resultado) {
        var sync = resultado.sincronizacao();
        var pedido = resultado.pedidoVenda();
        return new PdvVendaProcessamentoResponse(
                sync.getId(), sync.getTerminalId(), sync.getOperacaoLocalId(), sync.getSerie(), sync.getNumeroLocal(),
                pedido.getId(), pedido.getNumero(), pedido.getStatus(), sync.getProcessadoEm(), resultado.repetida());
    }
}
