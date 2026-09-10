package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FiscalArquivoApplicationServiceTest {
    @Test
    void geraChaveDeterministicaSemCredenciaisOuTravessiaDeDiretorio() {
        UUID tenant = UUID.randomUUID();
        UUID documento = UUID.randomUUID();
        UUID processado = UUID.randomUUID();

        String chave = FiscalArquivoApplicationService.chave(
                tenant, documento, processado);

        assertEquals("tenants/" + tenant + "/fiscal/documentos/" + documento
                + "/processados/" + processado + ".xml", chave);
        assertFalse(chave.contains(".."));
        assertFalse(chave.contains("secret"));
    }
}
