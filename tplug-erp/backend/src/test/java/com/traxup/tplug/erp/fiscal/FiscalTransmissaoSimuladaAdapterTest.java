package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FiscalTransmissaoSimuladaAdapterTest {
    private final FiscalTransmissaoSimuladaAdapter adapter =
            new FiscalTransmissaoSimuladaAdapter();

    @Test
    void retornaAutorizacaoDeterministicaExplicitamenteSimulada() throws Exception {
        String conteudo = "<xml><!-- assinatura SIMULADA --></xml>";
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(conteudo.getBytes(StandardCharsets.UTF_8)));
        var comando = new FiscalTransmissaoPort.Comando(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                conteudo, hash);

        var primeira = adapter.transmitir(comando);
        var segunda = adapter.transmitir(comando);

        assertEquals(primeira, segunda);
        assertEquals("SIMULADO", primeira.provedor());
        assertEquals("AUTORIZADO_SIMULADO", primeira.status());
        assertEquals("100-SIM", primeira.codigoResposta());
        assertTrue(primeira.protocolo().matches("SIM-[0-9a-f]{32}"));
        assertTrue(primeira.mensagemResposta().contains("sem validade fiscal"));
        assertEquals(64, primeira.hashResposta().length());
    }

    @Test
    void rejeitaConteudoComHashDivergente() {
        var comando = new FiscalTransmissaoPort.Comando(
                UUID.randomUUID(), UUID.randomUUID(), "<xml/>", "0".repeat(64));

        assertThrows(IllegalArgumentException.class, () -> adapter.transmitir(comando));
    }
}
