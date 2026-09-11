package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpedProntidaoApplicationServiceTest {
    @Test
    void informaCadaPendenciaSemExporConfiguracaoInterna() {
        var desabilitado = SpedProntidaoApplicationService
                .calcular(false, false, false);
        assertFalse(desabilitado.prontoParaProcessar());
        assertEquals("WORKER_DESABILITADO",
                desabilitado.pendenciaCodigo());

        var semGerador = SpedProntidaoApplicationService
                .calcular(true, false, true);
        assertEquals("GERADOR_HOMOLOGADO_NAO_CONFIGURADO",
                semGerador.pendenciaCodigo());

        var semRepositorio = SpedProntidaoApplicationService
                .calcular(true, true, false);
        assertEquals("REPOSITORIO_NAO_CONFIGURADO",
                semRepositorio.pendenciaCodigo());
    }

    @Test
    void ficaProntoSomenteComTodosOsComponentesAtivos() {
        var prontidao = SpedProntidaoApplicationService
                .calcular(true, true, true);

        assertTrue(prontidao.prontoParaProcessar());
        assertTrue(prontidao.workerHabilitado());
        assertTrue(prontidao.geradorHomologadoConfigurado());
        assertTrue(prontidao.repositorioConfigurado());
        assertNull(prontidao.pendenciaCodigo());
    }
}
