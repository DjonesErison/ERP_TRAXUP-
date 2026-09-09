package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FiscalAssinaturaSimuladaAdapterTest {
    private final FiscalAssinaturaSimuladaAdapter adapter = new FiscalAssinaturaSimuladaAdapter();

    @Test
    void produzArtefatoDeterministicoExplicitamenteSimulado() {
        var comando = new FiscalAssinaturaPort.Comando(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "a".repeat(64), "<?xml version=\"1.0\"?><TraxUPFiscal versao=\"1.2\"/>");

        var primeira = adapter.assinar(comando);
        var segunda = adapter.assinar(comando);

        assertEquals("SIMULADA", primeira.tipo());
        assertEquals("SIMULADO_SHA256", primeira.algoritmo());
        assertEquals(primeira, segunda);
        assertEquals(64, primeira.hashSha256().length());
        assertTrue(primeira.conteudoAssinado().contains("assinatura SIMULADA de homologacao"));
        assertFalse(primeira.conteudoAssinado().contains(comando.thumbprintSha256()));
    }

    @Test
    void rejeitaXmlVazio() {
        var comando = new FiscalAssinaturaPort.Comando(
                UUID.randomUUID(), UUID.randomUUID(), "b".repeat(64), " ");

        assertThrows(IllegalArgumentException.class, () -> adapter.assinar(comando));
    }
}
