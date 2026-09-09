package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FiscalCertificadoApplicationServiceTest {
    private FiscalCertificadoApplicationService service;

    @BeforeEach
    void setUp() {
        service = new FiscalCertificadoApplicationService(
                mock(JdbcTemplate.class), mock(AuditoriaApplicationService.class));
    }

    @Test
    void normalizaTipoDeCertificado() {
        assertEquals("A1", service.normalizarTipo(" a1 "));
        assertEquals("A3", service.normalizarTipo("a3"));
    }

    @Test
    void rejeitaTipoNaoSuportado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.normalizarTipo("A2"));
    }

    @Test
    void normalizaCpfECnpjComPontuacao() {
        assertEquals("12345678901", service.somenteDigitos("123.456.789-01"));
        assertEquals("12345678000199", service.somenteDigitos("12.345.678/0001-99"));
    }

    @Test
    void rejeitaDocumentoComTamanhoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.somenteDigitos("123"));
    }

    @Test
    void normalizaThumbprintParaMinusculas() {
        assertEquals("abcdef".repeat(10) + "abcd",
                service.normalizarThumbprint("ABCDEF".repeat(10) + "ABCD"));
    }

    @Test
    void rejeitaThumbprintQueNaoSejaSha256Hexadecimal() {
        assertThrows(IllegalArgumentException.class,
                () -> service.normalizarThumbprint("z".repeat(64)));
    }
}
