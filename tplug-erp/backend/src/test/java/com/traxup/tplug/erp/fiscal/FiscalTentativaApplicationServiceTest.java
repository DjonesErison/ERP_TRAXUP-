package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FiscalTentativaApplicationServiceTest {
    @Test
    void materialDoHashECanonicoParaValoresDecimaisEquivalentes() {
        String primeiro = FiscalTentativaApplicationService.materialHash(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "5102", null, "102",
                new BigDecimal("10.00"), new BigDecimal("0.0"), new BigDecimal("10.000"));
        String segundo = FiscalTentativaApplicationService.materialHash(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "5102", null, "102",
                new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.TEN);

        assertEquals(primeiro, segundo);
    }

    @Test
    void alteracaoFiscalMudaMaterialDoHash() {
        String original = FiscalTentativaApplicationService.materialHash(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "5102", null, "102",
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN);
        String corrigido = FiscalTentativaApplicationService.materialHash(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "6102", null, "102",
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN);

        assertNotEquals(original, corrigido);
    }
}
