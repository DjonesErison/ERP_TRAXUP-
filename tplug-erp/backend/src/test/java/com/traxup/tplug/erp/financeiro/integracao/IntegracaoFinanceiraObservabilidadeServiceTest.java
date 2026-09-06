package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IntegracaoFinanceiraObservabilidadeServiceTest {
    @Test
    void deveIsolarConsultaPorTenant() {
        UUID tenant = UUID.randomUUID(); UUID integracaoId = UUID.randomUUID();
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        when(integracoes.findByIdAndTenantId(integracaoId, tenant)).thenReturn(Optional.empty());
        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);
        assertThrows(RecursoNaoEncontradoException.class, () -> service.listar(tenant, integracaoId));
        verifyNoInteractions(tentativas);
    }

    @Test
    void deveRegistrarFalhaSemPersistirMensagemDoErro() {
        UUID tenant = UUID.randomUUID();
        var integracao = new IntegracaoFinanceira(tenant, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, UUID.randomUUID());
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);
        var captor = org.mockito.ArgumentCaptor.forClass(IntegracaoFinanceiraTentativa.class);
        Instant agora = Instant.now();
        service.falha(integracao, 3, 10, new IllegalStateException("token=segredo"), agora, agora.plusMillis(10));
        verify(tentativas).save(captor.capture());
        assertEquals("FALHA", captor.getValue().getStatus());
        assertEquals("IllegalStateException", captor.getValue().getErroCodigo());
        assertFalse(captor.getValue().getErroCodigo().contains("segredo"));
        assertEquals(tenant, captor.getValue().getTenantId());
    }

    @Test
    void deveResumirSaudeComFalhasConsecutivasSemCruzarTenant() {
        UUID tenant = UUID.randomUUID(); UUID integracaoId = UUID.randomUUID();
        var integracao = new IntegracaoFinanceira(tenant, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, UUID.randomUUID());
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        when(integracoes.findByIdAndTenantId(integracaoId, tenant)).thenReturn(Optional.of(integracao));
        Instant base = Instant.parse("2026-09-06T12:00:00Z");
        var falha1 = IntegracaoFinanceiraTentativa.falha(tenant, integracaoId, "BANCO-X", 0, 10,
                new IllegalStateException(), base, base.plusSeconds(1));
        var falha2 = IntegracaoFinanceiraTentativa.falha(tenant, integracaoId, "BANCO-X", 0, 10,
                new IllegalStateException(), base.minusSeconds(10), base.minusSeconds(9));
        var sucesso = IntegracaoFinanceiraTentativa.sucesso(tenant, integracaoId, "BANCO-X", 2, 10,
                base.minusSeconds(20), base.minusSeconds(19));
        when(tentativas.findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(tenant, integracaoId))
                .thenReturn(List.of(falha1, falha2, sucesso));

        var resumo = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes)
                .resumirSaude(tenant, integracaoId);

        assertEquals("ATENCAO", resumo.status());
        assertEquals(2, resumo.falhasConsecutivas());
        assertEquals(sucesso.getFinalizadoEm(), resumo.ultimoSucessoEm());
        assertEquals(3, resumo.tentativasConsideradas());
        verify(integracoes).findByIdAndTenantId(integracaoId, tenant);
    }

    @Test
    void deveMarcarSemExecucaoQuandoNaoHaTentativas() {
        UUID tenant = UUID.randomUUID(); UUID integracaoId = UUID.randomUUID();
        var integracao = new IntegracaoFinanceira(tenant, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, UUID.randomUUID());
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        when(integracoes.findByIdAndTenantId(integracaoId, tenant)).thenReturn(Optional.of(integracao));
        when(tentativas.findTop50ByTenantIdAndIntegracaoIdOrderByIniciadoEmDesc(tenant, integracaoId)).thenReturn(List.of());

        var resumo = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes)
                .resumirSaude(tenant, integracaoId);

        assertEquals("SEM_EXECUCAO", resumo.status());
        assertEquals(0, resumo.falhasConsecutivas());
        assertNull(resumo.ultimaTentativaEm());
    }
}
