package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvConfiguracao;
import java.time.Instant;
import java.util.UUID;

public record PdvConfiguracaoResponse(UUID id, UUID filialId, UUID terminalId,
        boolean exigirJustificativaCancelamento, boolean exigirAutorizacaoCancelamento,
        String tamanhoImpressao, boolean imprimirCaixa, boolean imprimirCozinha,
        Instant criadoEm, Instant atualizadoEm) {
    public static PdvConfiguracaoResponse from(PdvConfiguracao c) {
        return new PdvConfiguracaoResponse(c.getId(), c.getFilialId(), c.getTerminalId(),
                c.isExigirJustificativaCancelamento(), c.isExigirAutorizacaoCancelamento(),
                c.getTamanhoImpressao(), c.isImprimirCaixa(), c.isImprimirCozinha(),
                c.getCriadoEm(), c.getAtualizadoEm());
    }
}
