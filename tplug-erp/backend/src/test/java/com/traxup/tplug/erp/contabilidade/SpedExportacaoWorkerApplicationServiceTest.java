package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpedExportacaoWorkerApplicationServiceTest {
    @Test
    void validaLimitesDeProcessamento() {
        assertEquals(5,
                SpedExportacaoWorkerApplicationService.normalizarLimite(null));
        assertEquals(20,
                SpedExportacaoWorkerApplicationService.normalizarLimite(20));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoWorkerApplicationService
                        .normalizarLimite(21));

        assertEquals(5,
                SpedExportacaoWorkerApplicationService
                        .normalizarMaxTentativas(5));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoWorkerApplicationService
                        .normalizarMaxTentativas(0));
    }

    @Test
    void calculaHashEChaveDeterministicos() {
        byte[] conteudo = "SPED-controlado".getBytes(StandardCharsets.UTF_8);
        assertEquals(
                "f6e9f502ca3c204b64f93eaaee108e595b2da1e8ea61d4eec6e3ba64ed92b157",
                SpedExportacaoWorkerApplicationService.sha256(conteudo));

        UUID tenantId = UUID.fromString(
                "10000000-0000-4000-8000-000000000001");
        var item = new SpedExportacaoWorkerApplicationService.Item(
                UUID.fromString("20000000-0000-4000-8000-000000000002"),
                UUID.fromString("30000000-0000-4000-8000-000000000003"),
                "EFD_ICMS_IPI", YearMonth.of(2026, 8));
        assertEquals(
                "tenants/10000000-0000-4000-8000-000000000001/"
                        + "filiais/30000000-0000-4000-8000-000000000003/"
                        + "sped/EFD_ICMS_IPI/2026-08/"
                        + "20000000-0000-4000-8000-000000000002.txt",
                SpedExportacaoWorkerApplicationService
                        .chaveObjeto(tenantId, item));
    }

    @Test
    void recusaArtefatoVazioSemProvedorOuSemVersaoDoLayout() {
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoWorkerApplicationService
                        .validarArtefato(new SpedGeradorPort.Artefato(
                                new byte[0], "PROVEDOR", "019")));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoWorkerApplicationService
                        .validarArtefato(new SpedGeradorPort.Artefato(
                                new byte[]{1}, " ", "019")));
        assertThrows(IllegalArgumentException.class,
                () -> SpedExportacaoWorkerApplicationService
                        .validarArtefato(new SpedGeradorPort.Artefato(
                                new byte[]{1}, "PROVEDOR", " ")));
    }
}
