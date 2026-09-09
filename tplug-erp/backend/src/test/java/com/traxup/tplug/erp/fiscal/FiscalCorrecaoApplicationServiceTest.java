package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FiscalCorrecaoApplicationServiceTest {
    @Test
    void normalizaEOrdenaValoresFiscaisPermitidos() {
        var valores = FiscalCorrecaoApplicationService.validarValores(Map.of(
                "tributacao.ufDestino", " pe ",
                "tributacao.cfop", "5102"));

        assertEquals("5102", valores.get("tributacao.cfop"));
        assertEquals("PE", valores.get("tributacao.ufDestino"));
    }

    @Test
    void rejeitaCampoNaoSuportadoOuValorNaoTextual() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalCorrecaoApplicationService.validarValores(
                        Map.of("certificado.senha", "segredo")));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalCorrecaoApplicationService.validarValores(
                        Map.of("tributacao.cfop", 5102)));
    }

    @Test
    void validaFormatosFiscais() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalCorrecaoApplicationService.validarValores(
                        Map.of("tributacao.cfop", "51A2")));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalCorrecaoApplicationService.validarValores(
                        Map.of("tributacao.ufDestino", "PERNAMBUCO")));
    }

    @Test
    void impedeCstECsosnNaMesmaRevisao() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalCorrecaoApplicationService.validarValores(Map.of(
                        "tributacao.cstIcms", "00",
                        "tributacao.csosn", "102")));
    }
}
