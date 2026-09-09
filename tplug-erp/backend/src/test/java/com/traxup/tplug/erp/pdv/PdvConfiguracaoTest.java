package com.traxup.tplug.erp.pdv;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PdvConfiguracaoTest {
    @Test
    void iniciaComPadroesSeguros() {
        PdvConfiguracao config = new PdvConfiguracao(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        assertTrue(config.isExigirJustificativaCancelamento());
        assertFalse(config.isExigirAutorizacaoCancelamento());
        assertEquals("MEDIA", config.getTamanhoImpressao());
        assertTrue(config.isImprimirCaixa());
        assertFalse(config.isImprimirCozinha());
    }

    @Test
    void atualizaParametrosOperacionais() {
        PdvConfiguracao config = new PdvConfiguracao(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        config.atualizar(false, true, "GRANDE", false, true);
        assertFalse(config.isExigirJustificativaCancelamento());
        assertTrue(config.isExigirAutorizacaoCancelamento());
        assertEquals("GRANDE", config.getTamanhoImpressao());
        assertFalse(config.isImprimirCaixa());
        assertTrue(config.isImprimirCozinha());
    }
}
