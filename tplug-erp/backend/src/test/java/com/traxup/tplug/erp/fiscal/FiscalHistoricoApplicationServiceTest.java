package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiscalHistoricoApplicationServiceTest {
    private static final UUID ID = UUID.randomUUID();

    @Test
    void identificaEtapaMaisAvancadaSemConfundirSimulacaoComAutorizacaoReal() {
        assertEquals("ESTRUTURADO",
                FiscalHistoricoApplicationService.determinarEtapa(null, null, null, null, null));
        assertEquals("NUMERADO",
                FiscalHistoricoApplicationService.determinarEtapa(1L, null, null, null, null));
        assertEquals("XML_GERADO",
                FiscalHistoricoApplicationService.determinarEtapa(1L, ID, null, null, null));
        assertEquals("ASSINADO_SIMULADO",
                FiscalHistoricoApplicationService.determinarEtapa(1L, ID, ID, null, null));
        assertEquals("TRANSMITIDO_SIMULADO",
                FiscalHistoricoApplicationService.determinarEtapa(1L, ID, ID, ID, null));
        assertEquals("PROCESSADO_SIMULADO",
                FiscalHistoricoApplicationService.determinarEtapa(1L, ID, ID, ID, ID));
    }
}
