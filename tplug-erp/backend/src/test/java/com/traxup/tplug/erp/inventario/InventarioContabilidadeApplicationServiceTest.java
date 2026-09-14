package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.inventario.api.InventarioContabilidadeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventarioContabilidadeApplicationServiceTest {
    @Test
    void validaCompetenciaLimiteEPaginacao() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);

        InventarioContabilidadeApplicationService
                .validarPeriodo(inicio, inicio.plusDays(365));
        assertEquals(100, InventarioContabilidadeApplicationService
                .validarLimite(null));
        assertEquals(3, InventarioContabilidadeApplicationService
                .totalPaginas(201, 100));
        assertEquals(1, InventarioContabilidadeApplicationService
                .totalPaginas(0, 100));
        InventarioContabilidadeApplicationService.validarPagina(3, 3);

        assertThrows(IllegalArgumentException.class,
                () -> InventarioContabilidadeApplicationService
                        .validarPeriodo(inicio, inicio.plusDays(366)));
        assertThrows(IllegalArgumentException.class,
                () -> InventarioContabilidadeApplicationService
                        .validarLimite(501));
        assertThrows(IllegalArgumentException.class,
                () -> InventarioContabilidadeApplicationService
                        .validarPagina(4, 3));
    }

    @Test
    void exigePermissaoContabilDedicada() throws NoSuchMethodException {
        PreAuthorize regra = InventarioContabilidadeController.class
                .getDeclaredMethod("consultar", LocalDate.class,
                        LocalDate.class, UUID.class, Integer.class, int.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('CONTABILIDADE_INVENTARIO_LER')",
                regra.value());
    }
}
