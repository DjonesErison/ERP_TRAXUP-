package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvVendaSincronizacao;

import java.time.Instant;
import java.util.UUID;

public record PdvVendaSincronizacaoResponse(
        UUID id,
        UUID terminalId,
        UUID operacaoLocalId,
        Integer serie,
        Long numeroLocal,
        Instant ocorridoEm,
        Instant recebidoEm,
        boolean repetida
) {
    public static PdvVendaSincronizacaoResponse from(PdvVendaSincronizacao sync, boolean repetida) {
        return new PdvVendaSincronizacaoResponse(sync.getId(), sync.getTerminalId(), sync.getOperacaoLocalId(),
                sync.getSerie(), sync.getNumeroLocal(), sync.getOcorridoEm(), sync.getRecebidoEm(), repetida);
    }
}
