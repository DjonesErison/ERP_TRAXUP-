package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalFalhasPendentesApplicationServiceTest {
    @Test
    void aplicaLimitePadraoEProtegeConsultaExcessiva() {
        assertEquals(50, FiscalFalhasPendentesApplicationService.normalizarLimite(null));
        assertEquals(1, FiscalFalhasPendentesApplicationService.normalizarLimite(1));
        assertEquals(100, FiscalFalhasPendentesApplicationService.normalizarLimite(100));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalFalhasPendentesApplicationService.normalizarLimite(0));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalFalhasPendentesApplicationService.normalizarLimite(101));
    }
}
