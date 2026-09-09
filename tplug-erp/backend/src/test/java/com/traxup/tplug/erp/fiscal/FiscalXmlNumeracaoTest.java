package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FiscalXmlNumeracaoTest {

    @Test
    void incluiSerieENumeroNoXmlInternoUmDois() {
        FiscalXmlApplicationService service = new FiscalXmlApplicationService(
                mock(JdbcTemplate.class), mock(AuditoriaApplicationService.class));
        var documento = new FiscalXmlApplicationService.Documento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "NFE", "HOMOLOGACAO", 12, 345L,
                new BigDecimal("100.00"), new BigDecimal("5.00"), new BigDecimal("95.00"),
                "VENDA", "SIMPLES_NACIONAL", "PE", "5102", null, "102");
        var item = new FiscalXmlApplicationService.Item(
                "P1", "Produto & Teste", "12345678", "UN",
                BigDecimal.ONE, new BigDecimal("100.00"), BigDecimal.ZERO,
                new BigDecimal("5.00"), new BigDecimal("95.00"));

        String xml = service.escrever(documento, List.of(item));

        assertTrue(xml.contains("versao=\"1.2\""));
        assertTrue(xml.contains("<serie>12</serie>"));
        assertTrue(xml.contains("<numero>345</numero>"));
        assertTrue(xml.contains("<descricao>Produto &amp; Teste</descricao>"));
    }
}
