package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalTentativasParadasApplicationServiceTest {
    private static final UUID ID = UUID.randomUUID();

    @Test
    void validaJanelaDeDeteccao() {
        assertEquals(15, FiscalTentativasParadasApplicationService.normalizarMinutos(null));
        assertEquals(5, FiscalTentativasParadasApplicationService.normalizarMinutos(5));
        assertEquals(1440, FiscalTentativasParadasApplicationService.normalizarMinutos(1440));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalTentativasParadasApplicationService.normalizarMinutos(4));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalTentativasParadasApplicationService.normalizarMinutos(1441));
    }

    @Test
    void identificaUltimaEtapaConcluida() {
        assertEquals("INICIADA",
                FiscalTentativasParadasApplicationService.etapa(null, null, null, null));
        assertEquals("XML_GERADO",
                FiscalTentativasParadasApplicationService.etapa(ID, null, null, null));
        assertEquals("ASSINADO",
                FiscalTentativasParadasApplicationService.etapa(ID, ID, null, null));
        assertEquals("TRANSMITIDO",
                FiscalTentativasParadasApplicationService.etapa(ID, ID, ID, null));
        assertEquals("PROCESSADO",
                FiscalTentativasParadasApplicationService.etapa(ID, ID, ID, ID));
    }
}
