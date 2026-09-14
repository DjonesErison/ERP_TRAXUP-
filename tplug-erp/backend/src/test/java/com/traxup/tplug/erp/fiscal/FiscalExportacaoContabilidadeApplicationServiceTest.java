package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiscalExportacaoContabilidadeApplicationServiceTest {
    @Test
    void validaPeriodoMensalLimitado() {
        LocalDate inicio = LocalDate.of(2026, 8, 1);
        LocalDate fim = LocalDate.of(2026, 8, 31);

        FiscalExportacaoContabilidadeApplicationService
                .validarPeriodo(inicio, fim);
        assertThrows(IllegalArgumentException.class,
                () -> FiscalExportacaoContabilidadeApplicationService
                        .validarPeriodo(fim, inicio));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalExportacaoContabilidadeApplicationService
                        .validarPeriodo(inicio, inicio.plusDays(32)));
    }

    @Test
    void geraNomesControladosParaZipEXml() {
        UUID id = UUID.randomUUID();
        var arquivo = new FiscalExportacaoContabilidadeApplicationService.Arquivo(
                id, UUID.randomUUID(), "interna", "a".repeat(64),
                "NF-e/../../", 1, 99L,
                Instant.parse("2026-08-15T10:30:00Z"));

        String entrada = FiscalExportacaoContabilidadeApplicationService
                .nomeEntrada(arquivo);
        assertEquals("NF-E_______-1-99-" + id + ".xml", entrada);
        assertFalse(entrada.contains(".."));
        assertEquals("traxup-xml-2026-08-01-a-2026-08-31.zip",
                FiscalExportacaoContabilidadeApplicationService.nomeZip(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)));
    }

    @Test
    void geraManifestoVerificavelComDataFiscalSemExporChaveDeArmazenamento() {
        UUID arquivoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        String hash = "b".repeat(64);
        Instant dataFiscal = Instant.parse("2026-08-15T10:30:00Z");
        var arquivo = new FiscalExportacaoContabilidadeApplicationService.Arquivo(
                arquivoId, documentoId, "tenant/secreto/documento.xml", hash,
                "NF-e", 2, 123L, dataFiscal);

        String manifesto = FiscalExportacaoContabilidadeApplicationService
                .manifesto(List.of(arquivo));

        assertTrue(manifesto.startsWith(
                "arquivo_id;documento_id;modelo;serie;numero;data_fiscal;"
                        + "hash_sha256;nome_arquivo\n"));
        assertTrue(manifesto.contains("\"" + arquivoId + "\""));
        assertTrue(manifesto.contains("\"" + documentoId + "\""));
        assertTrue(manifesto.contains("\"" + dataFiscal + "\""));
        assertTrue(manifesto.contains("\"" + hash + "\""));
        assertTrue(manifesto.contains(
                "\"NF-E-2-123-" + arquivoId + ".xml\""));
        assertFalse(manifesto.contains("tenant/secreto"));
    }
}
