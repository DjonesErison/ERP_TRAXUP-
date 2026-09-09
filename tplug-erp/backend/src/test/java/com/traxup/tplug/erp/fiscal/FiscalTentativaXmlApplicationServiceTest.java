package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FiscalTentativaXmlApplicationServiceTest {
    @Test
    void hashConfereComSnapshotCanonicoDaTentativa() {
        String hash = FiscalTentativaXmlApplicationService.calcularHashDocumento(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "5102", null, "102",
                new BigDecimal("10.00"), BigDecimal.ZERO, new BigDecimal("10.0"));

        assertEquals(64, hash.length());
        assertEquals(hash, FiscalTentativaXmlApplicationService.calcularHashDocumento(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "5102", null, "102",
                BigDecimal.TEN, new BigDecimal("0.000"), BigDecimal.TEN));
    }

    @Test
    void hashMudaQuandoDocumentoEAlteradoDepoisDaTentativa() {
        String original = FiscalTentativaXmlApplicationService.calcularHashDocumento(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "PE", "5102", null, "102",
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN);
        String alterado = FiscalTentativaXmlApplicationService.calcularHashDocumento(
                "NFE", "HOMOLOGACAO", 1, 10L, "SAIDA", "SIMPLES_NACIONAL",
                "SP", "5102", null, "102",
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN);

        assertNotEquals(original, alterado);
    }
}
