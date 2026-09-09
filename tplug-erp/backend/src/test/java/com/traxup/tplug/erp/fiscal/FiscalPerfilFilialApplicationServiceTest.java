package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.filial.Filial;
import com.traxup.tplug.erp.filial.FilialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FiscalPerfilFilialApplicationServiceTest {
    private FiscalPerfilFilialRepository repository;
    private FilialRepository filialRepository;
    private AuditoriaApplicationService auditoria;
    private FiscalPerfilFilialApplicationService service;

    @BeforeEach
    void setUp() {
        repository = mock(FiscalPerfilFilialRepository.class);
        filialRepository = mock(FilialRepository.class);
        auditoria = mock(AuditoriaApplicationService.class);
        service = new FiscalPerfilFilialApplicationService(repository, filialRepository, auditoria);
    }

    @Test
    void criaPerfilNormalizadoComAuditoria() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        Filial filial = filialAtiva(empresaId);
        when(filialRepository.findByIdAndTenantId(filialId, tenantId)).thenReturn(Optional.of(filial));
        when(repository.findByTenantIdAndFilialId(tenantId, filialId)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(FiscalPerfilFilial.class))).thenAnswer(i -> i.getArgument(0));

        FiscalPerfilFilial perfil = service.salvar(
                tenantId, usuarioId, filialId, "simples nacional", (short) 1, "producao", 1, 2);

        assertEquals("SIMPLES_NACIONAL", perfil.getRegimeTributario());
        assertEquals("PRODUCAO", perfil.getAmbiente());
        assertEquals(1, perfil.getSerieNfe());
        assertEquals(2, perfil.getSerieNfce());
        assertTrue(perfil.isAtivo());
        verify(auditoria).registrar(eq(tenantId), eq(usuarioId), eq(empresaId), eq(filialId),
                eq("SALVAR_PERFIL"), eq("FISCAL_PERFIL_FILIAL"), eq(perfil.getId()), anyString());
    }

    @Test
    void atualizaPerfilExistenteSemDuplicar() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        FiscalPerfilFilial existente = new FiscalPerfilFilial(
                tenantId, filialId, "SIMPLES_NACIONAL", (short) 1, "HOMOLOGACAO", 1, 1);
        Filial filial = filialAtiva(UUID.randomUUID());
        when(filialRepository.findByIdAndTenantId(filialId, tenantId))
                .thenReturn(Optional.of(filial));
        when(repository.findByTenantIdAndFilialId(tenantId, filialId)).thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(existente)).thenReturn(existente);

        FiscalPerfilFilial perfil = service.salvar(
                tenantId, null, filialId, "regime-normal", (short) 3, "producao", 10, 20);

        assertSame(existente, perfil);
        assertEquals("REGIME_NORMAL", perfil.getRegimeTributario());
        assertEquals(3, perfil.getCrt());
        assertEquals(10, perfil.getSerieNfe());
        verify(repository).saveAndFlush(existente);
    }

    @Test
    void rejeitaCrtIncompativelComRegime() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Filial filial = filialAtiva(UUID.randomUUID());
        when(filialRepository.findByIdAndTenantId(filialId, tenantId))
                .thenReturn(Optional.of(filial));

        assertThrows(IllegalArgumentException.class, () -> service.salvar(
                tenantId, null, filialId, "SIMPLES_NACIONAL", (short) 3, "PRODUCAO", 1, 1));
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(auditoria);
    }

    @Test
    void naoAcessaFilialDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(filialRepository.findByIdAndTenantId(filialId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscar(tenantId, filialId));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaSerieForaDoIntervaloFiscal() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Filial filial = filialAtiva(UUID.randomUUID());
        when(filialRepository.findByIdAndTenantId(filialId, tenantId))
                .thenReturn(Optional.of(filial));

        assertThrows(IllegalArgumentException.class, () -> service.salvar(
                tenantId, null, filialId, "REGIME_NORMAL", (short) 3, "HOMOLOGACAO", 0, 1));
        verify(repository, never()).saveAndFlush(any());
    }

    private Filial filialAtiva(UUID empresaId) {
        Filial filial = mock(Filial.class);
        Empresa empresa = mock(Empresa.class);
        when(filial.isAtivo()).thenReturn(true);
        when(filial.getEmpresa()).thenReturn(empresa);
        when(empresa.getId()).thenReturn(empresaId);
        return filial;
    }
}
