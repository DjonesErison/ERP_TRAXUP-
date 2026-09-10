package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalTentativaProcessadoApplicationServiceTest {
    @Test
    void aceitaSomenteTentativaConcluidaComTransmissaoSimuladaAutorizada() {
        assertDoesNotThrow(() -> FiscalTentativaProcessadoApplicationService.validar(
                "CONCLUIDA", "HOMOLOGACAO", "SIMULADO", "AUTORIZADO_SIMULADO"));

        assertThrows(IllegalArgumentException.class,
                () -> FiscalTentativaProcessadoApplicationService.validar(
                        "EM_PROCESSAMENTO", "HOMOLOGACAO", "SIMULADO",
                        "AUTORIZADO_SIMULADO"));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalTentativaProcessadoApplicationService.validar(
                        "CONCLUIDA", "PRODUCAO", "SIMULADO", "AUTORIZADO_SIMULADO"));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalTentativaProcessadoApplicationService.validar(
                        "CONCLUIDA", "HOMOLOGACAO", "SEFAZ", "AUTORIZADO"));
    }
}
