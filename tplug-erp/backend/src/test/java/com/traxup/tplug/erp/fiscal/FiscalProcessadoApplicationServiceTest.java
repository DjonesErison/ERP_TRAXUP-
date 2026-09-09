package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FiscalProcessadoApplicationServiceTest {
    @Test
    void geraEnvelopeValidoEExplicitamenteSimulado() {
        var service = new FiscalProcessadoApplicationService(
                mock(JdbcTemplate.class), mock(com.traxup.tplug.erp.auditoria.AuditoriaApplicationService.class));
        var origem = new FiscalProcessadoApplicationService.Origem(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "AUTORIZADO_SIMULADO", "100-SIM",
                "Autorizacao simulada de homologacao; sem validade fiscal",
                "SIM-" + "a".repeat(32), "b".repeat(64),
                "<?xml version=\"1.0\"?><TraxUPFiscal versao=\"1.2\"/>",
                UUID.randomUUID());

        String xml = service.escrever(origem);

        assertTrue(xml.contains("tipo=\"PROCESSADO_SIMULADO\""));
        assertTrue(xml.contains("<numero>SIM-" + "a".repeat(32) + "</numero>"));
        assertTrue(xml.contains("Artefato simulado sem validade fiscal"));
        assertTrue(xml.contains("&lt;?xml version="));
        assertFalse(xml.contains("<procNFe"));
    }
}
