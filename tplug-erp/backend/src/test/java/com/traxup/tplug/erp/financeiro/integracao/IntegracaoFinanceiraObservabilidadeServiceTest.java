package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void deveNormalizarStatusEAplicarPeriodoSemRemoverEscopoDoTenant() {
        UUID tenant = UUID.randomUUID(); UUID integracaoId = UUID.randomUUID();
        var integracao = new IntegracaoFinanceira(tenant, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, UUID.randomUUID());
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        Instant inicio = Instant.parse("2026-09-01T00:00:00Z");
        Instant fim = Instant.parse("2026-09-06T23:59:59Z");
        when(integracoes.findByIdAndTenantId(integracaoId, tenant)).thenReturn(Optional.of(integracao));
        when(tentativas.filtrar(eq(tenant), eq(integracaoId), eq("FALHA"), eq(inicio), eq(fim), any(Pageable.class)))
                .thenReturn(List.of());

        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);
        service.listar(tenant, integracaoId, " falha ", inicio, fim);

        verify(tentativas).filtrar(eq(tenant), eq(integracaoId), eq("FALHA"), eq(inicio), eq(fim), any(Pageable.class));
    }

    @Test
    void deveRejeitarPeriodoInvertidoAntesDeConsultarRepositorios() {
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);
        Instant inicio = Instant.parse("2026-09-06T10:00:00Z");
        Instant fim = inicio.minusSeconds(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), UUID.randomUUID(), null, inicio, fim));
        verifyNoInteractions(tentativas, integracoes);
    }

    @Test
    void deveRejeitarStatusDesconhecidoAntesDeConsultarRepositorios() {
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        var service = new IntegracaoFinanceiraObservabilidadeService(tentativas, integracoes);

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), UUID.randomUUID(), "ERRO", null, null));
        verifyNoInteractions(tentativas, integracoes);
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
    void deveResumirSaudeComFalhasConsecutivasEMetricasSemCruzarTenant() {
        UUID tenant = UUID.randomUUID(); UUID integracaoId = UUID.randomUUID();
        var integracao = new IntegracaoFinanceira(tenant, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, UUID.randomUUID());
        var tentativas = mock(IntegracaoFinanceiraTentativaRepository.class);
        var integracoes = mock(IntegracaoFinanceiraRepository.class);
        when(integracoes.findByIdAndTenantId(integracaoId, tenant)).thenReturn(Optional.of(integracao));
        Instant base = Instant.parse("2026-09-06T12:00:00Z");
        var falha1 = IntegracaoFinanceiraTentativa.falha(tenant, integracaoId, "BANCO-X", 0, 30,
                new IllegalStateException(), base, base.plusSeconds(1));
        var falha2 = IntegracaoFinanceiraTentativa.falha(tenant, integracaoId, "BANCO-X", 0, 20,
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
        assertEquals(1, resumo.sucessos());
        assertEquals(2, resumo.falhas());
        assertEquals(20L, resumo.duracaoMediaMs());
        verify(integracoes).findByIdAndTenantId(integracaoId, tenant);
    }

    @Test
    void deveMarcarSemExecucaoSemInventarMetricas() {
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
        assertEquals(0, resumo.sucessos());
        assertEquals(0, resumo.falhas());
        assertNull(resumo.duracaoMediaMs());
        assertNull(resumo.ultimaTentativaEm());
    }
}
