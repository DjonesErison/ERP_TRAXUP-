package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpedExportacaoSchedulerTest {
    @Test
    void validaLoteDeTenants() {
        assertEquals(1, SpedExportacaoScheduler.normalizarTenantBatch(1));
        assertEquals(200, SpedExportacaoScheduler.normalizarTenantBatch(200));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoScheduler.normalizarTenantBatch(0));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoScheduler.normalizarTenantBatch(201));
    }
}
