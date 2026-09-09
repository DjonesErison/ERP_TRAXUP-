package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FiscalValidacaoCertificadoTest {
    private FiscalValidacaoApplicationService service;
    private OffsetDateTime agora;

    @BeforeEach
    void setUp() {
        service = new FiscalValidacaoApplicationService(
                mock(JdbcTemplate.class), mock(FiscalPerfilFilialRepository.class));
        agora = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @Test
    void informaCertificadoNaoConfigurado() {
        var pendencias = service.validarCertificado(Optional.empty(), agora);

        assertEquals(1, pendencias.size());
        assertEquals("CERTIFICADO_FISCAL_NAO_CONFIGURADO",
                pendencias.getFirst().codigo());
    }

    @Test
    void informaCertificadoAindaNaoValido() {
        var certificado = new FiscalValidacaoApplicationService.Certificado(
                agora.plusDays(1), agora.plusYears(1));

        var pendencias = service.validarCertificado(Optional.of(certificado), agora);

        assertEquals("CERTIFICADO_FISCAL_AINDA_NAO_VALIDO",
                pendencias.getFirst().codigo());
    }

    @Test
    void informaCertificadoExpirado() {
        var certificado = new FiscalValidacaoApplicationService.Certificado(
                agora.minusYears(1), agora.minusSeconds(1));

        var pendencias = service.validarCertificado(Optional.of(certificado), agora);

        assertEquals("CERTIFICADO_FISCAL_EXPIRADO",
                pendencias.getFirst().codigo());
    }

    @Test
    void aceitaCertificadoDentroDaValidade() {
        var certificado = new FiscalValidacaoApplicationService.Certificado(
                agora.minusDays(1), agora.plusYears(1));

        var pendencias = service.validarCertificado(Optional.of(certificado), agora);

        assertTrue(pendencias.isEmpty());
    }
}
