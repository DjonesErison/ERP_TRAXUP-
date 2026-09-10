package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiscalArquivoWorkerApplicationServiceTest {
    @Test
    void validaLimiteDoLote() {
        assertEquals(10, FiscalArquivoWorkerApplicationService.normalizarLimite(null));
        assertEquals(1, FiscalArquivoWorkerApplicationService.normalizarLimite(1));
        assertEquals(20, FiscalArquivoWorkerApplicationService.normalizarLimite(20));
        assertThrows(IllegalArgumentException.class,
                () -> FiscalArquivoWorkerApplicationService.normalizarLimite(21));
    }

    @Test
    void recusaConteudoAlteradoAntesDoUpload() throws Exception {
        String conteudo = "<xml>seguro</xml>";
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(conteudo.getBytes(StandardCharsets.UTF_8)));

        assertTrue(FiscalArquivoWorkerApplicationService.hashValido(conteudo, hash));
        assertFalse(FiscalArquivoWorkerApplicationService.hashValido(
                conteudo + "alterado", hash));
    }
}
