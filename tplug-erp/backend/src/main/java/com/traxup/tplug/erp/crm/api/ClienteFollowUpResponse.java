package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.crm.ClienteFollowUp;

import java.time.Instant;
import java.util.UUID;

public record ClienteFollowUpResponse(
        UUID id,
        UUID filialId,
        UUID clienteId,
        String status,
        String assunto,
        String observacao,
        Instant agendadoPara,
        UUID criadoPorId,
        UUID concluidoPorId,
        Instant concluidoEm,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ClienteFollowUpResponse from(ClienteFollowUp followUp) {
        return new ClienteFollowUpResponse(
                followUp.getId(), followUp.getFilialId(), followUp.getClienteId(), followUp.getStatus(),
                followUp.getAssunto(), followUp.getObservacao(), followUp.getAgendadoPara(),
                followUp.getCriadoPorId(), followUp.getConcluidoPorId(), followUp.getConcluidoEm(),
                followUp.getCriadoEm(), followUp.getAtualizadoEm());
    }
}
