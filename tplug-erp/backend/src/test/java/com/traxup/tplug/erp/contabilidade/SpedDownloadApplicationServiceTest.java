package com.traxup.tplug.erp.contabilidade;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpedDownloadApplicationServiceTest {
    @Test
    void validaHashAntesDeEntregarArquivo() {
        byte[] conteudo = "SPED-controlado".getBytes(StandardCharsets.UTF_8);
        String hash = SpedExportacaoWorkerApplicationService.sha256(conteudo);

        assertTrue(SpedDownloadApplicationService
                .hashConfere(conteudo, hash));
        assertFalse(SpedDownloadApplicationService
                .hashConfere("alterado".getBytes(StandardCharsets.UTF_8), hash));
        assertFalse(SpedDownloadApplicationService
                .hashConfere(conteudo, null));
    }

    @Test
    void geraNomeSemUsarChaveInternaDoStorage() {
        UUID id = UUID.fromString(
                "20000000-0000-4000-8000-000000000002");
        assertEquals(
                "sped-efd-icms-ipi-2026-08-"
                        + "20000000-0000-4000-8000-000000000002.txt",
                SpedDownloadApplicationService.nomeArquivo(
                        "EFD_ICMS_IPI", YearMonth.of(2026, 8), id));
    }
}
