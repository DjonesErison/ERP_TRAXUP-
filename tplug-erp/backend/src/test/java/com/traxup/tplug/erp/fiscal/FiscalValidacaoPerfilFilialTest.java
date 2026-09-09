package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FiscalValidacaoPerfilFilialTest {
    private FiscalPerfilFilialRepository perfis;
    private FiscalValidacaoApplicationService service;

    @BeforeEach
    void setUp() {
        perfis = mock(FiscalPerfilFilialRepository.class);
        service = new FiscalValidacaoApplicationService(mock(JdbcTemplate.class), perfis);
    }

    @Test
    void informaPerfilNaoConfigurado() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(perfis.findByTenantIdAndFilialIdAndAtivoTrue(tenantId, filialId))
                .thenReturn(Optional.empty());

        var pendencias = service.validarPerfil(
                tenantId, filialId, "HOMOLOGACAO", "SIMPLES_NACIONAL");

        assertEquals(1, pendencias.size());
        assertEquals("PERFIL_FISCAL_NAO_CONFIGURADO", pendencias.getFirst().codigo());
    }

    @Test
    void informaAmbienteERegimeDivergentes() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        FiscalPerfilFilial perfil = new FiscalPerfilFilial(
                tenantId, filialId, "REGIME_NORMAL", (short) 3, "PRODUCAO", 1, 1);
        when(perfis.findByTenantIdAndFilialIdAndAtivoTrue(tenantId, filialId))
                .thenReturn(Optional.of(perfil));

        var pendencias = service.validarPerfil(
                tenantId, filialId, "HOMOLOGACAO", "SIMPLES_NACIONAL");

        assertEquals(2, pendencias.size());
        assertTrue(pendencias.stream().anyMatch(p -> "AMBIENTE_FISCAL_DIVERGENTE".equals(p.codigo())));
        assertTrue(pendencias.stream().anyMatch(p -> "REGIME_FISCAL_DIVERGENTE".equals(p.codigo())));
    }

    @Test
    void aceitaPerfilCoerenteComDocumento() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        FiscalPerfilFilial perfil = new FiscalPerfilFilial(
                tenantId, filialId, "SIMPLES_NACIONAL", (short) 1, "HOMOLOGACAO", 1, 1);
        when(perfis.findByTenantIdAndFilialIdAndAtivoTrue(tenantId, filialId))
                .thenReturn(Optional.of(perfil));

        var pendencias = service.validarPerfil(
                tenantId, filialId, "HOMOLOGACAO", "SIMPLES_NACIONAL");

        assertTrue(pendencias.isEmpty());
    }
}
