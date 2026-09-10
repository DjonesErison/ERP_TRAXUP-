package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalRepositorioConsultaApplicationServiceTest {
    @Test
    void aplicaFiltrosPadraoESanitizaStatus() {
        LocalDate hoje = LocalDate.now();
        var padrao = FiscalRepositorioConsultaApplicationService
                .normalizar(null, null, null, null);
        assertEquals(hoje, padrao.fim());
        assertEquals(hoje.minusDays(30), padrao.inicio());
        assertEquals(50, padrao.limite());
        assertNull(padrao.status());

        var filtrado = FiscalRepositorioConsultaApplicationService
                .normalizar(hoje.minusDays(1), hoje, " arquivado ", 200);
        assertEquals("ARQUIVADO", filtrado.status());
        assertEquals(200, filtrado.limite());
    }

    @Test
    void recusaPeriodoStatusELimiteInvalidos() {
        LocalDate hoje = LocalDate.now();
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRepositorioConsultaApplicationService.normalizar(
                        hoje, hoje.minusDays(1), null, 50));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRepositorioConsultaApplicationService.normalizar(
                        hoje.minusDays(367), hoje, null, 50));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRepositorioConsultaApplicationService.normalizar(
                        hoje, hoje, "EXCLUIDO", 50));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalRepositorioConsultaApplicationService.normalizar(
                        hoje, hoje, null, 201));
    }
}
