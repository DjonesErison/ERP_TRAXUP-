package com.traxup.tplug.erp.financeiro.integracao;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IntegracaoFinanceiraPainelOperacionalServiceTest {

    @Test
    void deveConsolidarSomenteIntegracoesRetornadasNoEscopoDaContaETenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        IntegracaoFinanceira primeira = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), contaId, "BANCO-X", null, usuarioId);
        IntegracaoFinanceira segunda = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), contaId, "PSP-Y", null, usuarioId);

        IntegracaoFinanceiraApplicationService integracaoService = mock(IntegracaoFinanceiraApplicationService.class);
        IntegracaoFinanceiraObservabilidadeService observabilidadeService = mock(IntegracaoFinanceiraObservabilidadeService.class);
        when(integracaoService.listar(tenantId, contaId)).thenReturn(List.of(primeira, segunda));
        when(observabilidadeService.resumirSaude(tenantId, primeira.getId())).thenReturn(
                new IntegracaoFinanceiraObservabilidadeService.ResumoSaude(
                        primeira.getId(), "SAUDAVEL", Instant.parse("2026-09-06T12:00:00Z"),
                        Instant.parse("2026-09-06T12:00:00Z"), 0, 3));
        when(observabilidadeService.resumirSaude(tenantId, segunda.getId())).thenReturn(
                new IntegracaoFinanceiraObservabilidadeService.ResumoSaude(
                        segunda.getId(), "ATENCAO", Instant.parse("2026-09-06T12:01:00Z"),
                        null, 2, 2));

        var service = new IntegracaoFinanceiraPainelOperacionalService(integracaoService, observabilidadeService);
        var painel = service.listar(tenantId, contaId);

        assertEquals(2, painel.size());
        assertEquals(primeira.getId(), painel.get(0).integracao().getId());
        assertEquals("SAUDAVEL", painel.get(0).saude().status());
        assertEquals(segunda.getId(), painel.get(1).integracao().getId());
        assertEquals("ATENCAO", painel.get(1).saude().status());
        verify(integracaoService).listar(tenantId, contaId);
        verify(observabilidadeService).resumirSaude(tenantId, primeira.getId());
        verify(observabilidadeService).resumirSaude(tenantId, segunda.getId());
        verifyNoMoreInteractions(integracaoService, observabilidadeService);
    }

    @Test
    void deveRetornarPainelVazioSemConsultarSaudeQuandoContaNaoPossuiIntegracoes() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        IntegracaoFinanceiraApplicationService integracaoService = mock(IntegracaoFinanceiraApplicationService.class);
        IntegracaoFinanceiraObservabilidadeService observabilidadeService = mock(IntegracaoFinanceiraObservabilidadeService.class);
        when(integracaoService.listar(tenantId, contaId)).thenReturn(List.of());

        var service = new IntegracaoFinanceiraPainelOperacionalService(integracaoService, observabilidadeService);

        assertEquals(List.of(), service.listar(tenantId, contaId));
        verify(integracaoService).listar(tenantId, contaId);
        verifyNoInteractions(observabilidadeService);
    }
}
