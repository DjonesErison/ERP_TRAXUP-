package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalArquivoSchedulerTest {
    @Test
    void protegeTamanhoDoLoteDeTenants() {
        assertEquals(1, FiscalArquivoScheduler.normalizarTenantBatch(1));
        assertEquals(200, FiscalArquivoScheduler.normalizarTenantBatch(200));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalArquivoScheduler.normalizarTenantBatch(0));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalArquivoScheduler.normalizarTenantBatch(201));
    }
}
