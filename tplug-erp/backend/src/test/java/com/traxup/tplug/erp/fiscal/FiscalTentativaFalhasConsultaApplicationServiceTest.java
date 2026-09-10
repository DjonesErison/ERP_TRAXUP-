package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiscalTentativaFalhasConsultaApplicationServiceTest {
    @Test
    void diferenciaFalhaPendenteDeFalhaResolvida() {
        assertEquals("PENDENTE",
                FiscalTentativaFalhasConsultaApplicationService.situacao(null));
        assertEquals("RESOLVIDA",
                FiscalTentativaFalhasConsultaApplicationService.situacao(Instant.now()));
    }
}
