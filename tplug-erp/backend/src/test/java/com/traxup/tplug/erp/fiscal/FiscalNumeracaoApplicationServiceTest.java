package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FiscalNumeracaoApplicationServiceTest {
    private FiscalNumeracaoApplicationService service;
    private FiscalPerfilFilial perfil;

    @BeforeEach
    void setUp() {
        service = new FiscalNumeracaoApplicationService(
                mock(JdbcTemplate.class),
                mock(FiscalPerfilFilialRepository.class),
                mock(FiscalValidacaoApplicationService.class),
                mock(AuditoriaApplicationService.class));
        perfil = new FiscalPerfilFilial(
                UUID.randomUUID(), UUID.randomUUID(), "SIMPLES_NACIONAL",
                (short) 1, "HOMOLOGACAO", 15, 25);
    }

    @Test
    void usaSerieNfeDoPerfil() {
        assertEquals(15, service.seriePara(perfil, "NFE"));
    }

    @Test
    void usaSerieNfceDoPerfil() {
        assertEquals(25, service.seriePara(perfil, "NFCE"));
    }

    @Test
    void rejeitaModeloDesconhecido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.seriePara(perfil, "CTE"));
    }
}
