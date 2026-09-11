package com.traxup.tplug.erp.inventario.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventarioContabilidadeSecurityTest {
    private static final String LEITURA =
            "hasAnyAuthority('INVENTARIO_LER', "
                    + "'CONTABILIDADE_INVENTARIO_LER')";

    @Test
    void permiteAutoridadeContabilSomenteNasConsultas()
            throws NoSuchMethodException {
        assertEquals(LEITURA, regra("listar",
                UUID.class, String.class, Integer.class));
        assertEquals(LEITURA, regra("buscar", UUID.class));
        assertEquals(LEITURA, regra("listarContagens",
                UUID.class, Integer.class));
        assertEquals(LEITURA, regra("listarDivergencias",
                UUID.class, Integer.class));

        assertEquals("hasAuthority('INVENTARIO_EDITAR')",
                regra("criar", CriarInventarioRequest.class));
        assertEquals("hasAuthority('INVENTARIO_EDITAR')",
                regra("registrarContagem", UUID.class,
                        RegistrarInventarioContagemRequest.class));
        assertEquals("hasAuthority('INVENTARIO_EDITAR')",
                regra("concluir", UUID.class));
        assertEquals("hasAuthority('INVENTARIO_AJUSTAR')",
                regra("ajustarEstoque", UUID.class));
        assertEquals("hasAuthority('INVENTARIO_EDITAR')",
                regra("cancelar", UUID.class));
    }

    private String regra(String metodo, Class<?>... parametros)
            throws NoSuchMethodException {
        return InventarioController.class
                .getDeclaredMethod(metodo, parametros)
                .getAnnotation(PreAuthorize.class)
                .value();
    }
}
