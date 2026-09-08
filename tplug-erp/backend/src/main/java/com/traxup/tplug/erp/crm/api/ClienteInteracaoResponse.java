package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.crm.ClienteInteracao;

import java.time.Instant;
import java.util.UUID;

public record ClienteInteracaoResponse(
        UUID id,
        UUID filialId,
        UUID clienteId,
        UUID followUpId,
        String canal,
        String resultado,
        String assunto,
        Instant ocorridoEm,
        UUID usuarioId,
        Instant criadoEm
) {
    public static ClienteInteracaoResponse from(ClienteInteracao i) {
        return new ClienteInteracaoResponse(i.getId(), i.getFilialId(), i.getClienteId(), i.getFollowUpId(),
                i.getCanal(), i.getResultado(), i.getAssunto(), i.getOcorridoEm(), i.getUsuarioId(), i.getCriadoEm());
    }
}
