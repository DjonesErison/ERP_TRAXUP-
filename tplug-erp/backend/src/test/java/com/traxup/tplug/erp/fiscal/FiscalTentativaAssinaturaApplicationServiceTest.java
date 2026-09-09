package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalTentativaAssinaturaApplicationServiceTest {
    @Test
    void aceitaSomenteTentativaEmProcessamentoHomologacaoXml12() {
        assertDoesNotThrow(() -> FiscalTentativaAssinaturaApplicationService
                .validar("EM_PROCESSAMENTO", "HOMOLOGACAO", "1.2"));

        assertThrows(IllegalArgumentException.class, () ->
                FiscalTentativaAssinaturaApplicationService
                        .validar("CRIADA", "HOMOLOGACAO", "1.2"));
        assertThrows(IllegalArgumentException.class, () ->
                FiscalTentativaAssinaturaApplicationService
                        .validar("EM_PROCESSAMENTO", "PRODUCAO", "1.2"));
        assertThrows(IllegalArgumentException.class, () ->
                FiscalTentativaAssinaturaApplicationService
                        .validar("EM_PROCESSAMENTO", "HOMOLOGACAO", "1.1"));
    }
}
