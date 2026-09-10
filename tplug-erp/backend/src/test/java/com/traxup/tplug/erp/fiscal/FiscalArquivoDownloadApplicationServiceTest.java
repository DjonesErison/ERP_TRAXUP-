package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiscalArquivoDownloadApplicationServiceTest {
    @Test
    void validaIntegridadeDepoisDoDownload() throws Exception {
        byte[] conteudo = "<xml>arquivado</xml>".getBytes(StandardCharsets.UTF_8);
        String hash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(conteudo));

        assertTrue(FiscalArquivoDownloadApplicationService.hashValido(
                conteudo, hash));
        assertFalse(FiscalArquivoDownloadApplicationService.hashValido(
                "<xml>alterado</xml>".getBytes(StandardCharsets.UTF_8), hash));
    }

    @Test
    void geraNomeDeDownloadSemEntradaDoUsuario() {
        String nome = FiscalArquivoDownloadApplicationService
                .nomeArquivo(UUID.randomUUID());

        assertTrue(nome.startsWith("traxup-fiscal-"));
        assertTrue(nome.endsWith(".xml"));
        assertFalse(nome.contains(".."));
    }
}
