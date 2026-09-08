package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvTerminal;

import java.time.Instant;
import java.util.UUID;

public record PdvTerminalResponse(
        UUID id,
        UUID filialId,
        String codigo,
        String nome,
        Integer serie,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PdvTerminalResponse from(PdvTerminal terminal) {
        return new PdvTerminalResponse(
                terminal.getId(), terminal.getFilialId(), terminal.getCodigo(), terminal.getNome(),
                terminal.getSerie(), terminal.isAtivo(), terminal.getCriadoEm(), terminal.getAtualizadoEm());
    }
}
