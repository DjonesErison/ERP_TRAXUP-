package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiscalPainelOperacionalApplicationServiceTest {
    @Test
    void priorizaFalhasSobreAlertasEOperacaoNormal() {
        assertEquals("NORMAL",
                FiscalPainelOperacionalApplicationService.saude(0, 0, 0));
        assertEquals("ATENCAO",
                FiscalPainelOperacionalApplicationService.saude(0, 1, 0));
        assertEquals("ATENCAO",
                FiscalPainelOperacionalApplicationService.saude(0, 0, 1));
        assertEquals("CRITICO",
                FiscalPainelOperacionalApplicationService.saude(1, 10, 10));
    }
}
