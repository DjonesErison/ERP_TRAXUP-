package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.financeiro.ConciliacaoApplicationService;
import com.traxup.tplug.erp.financeiro.ConciliacaoLancamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoFinanceiraSincronizacaoServiceTest {
    @Mock IntegracaoFinanceiraRepository repository;
    @Mock ConciliacaoApplicationService conciliacaoService;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveForcarOrigemDoProvedorEAvancarCheckpointDepoisDaImportacao() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID integracaoId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        IntegracaoFinanceira integracao = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), contaId, "psp_teste", "merchant", usuarioId);
        Instant sincronizadoEm = Instant.parse("2026-09-06T10:00:00Z");
        when(repository.findByIdAndTenantId(integracaoId, tenantId)).thenReturn(Optional.of(integracao));
        when(conciliacaoService.importarLote(eq(tenantId), eq(usuarioId), eq(contaId), anyList()))
                .thenReturn(List.of());
        when(repository.save(integracao)).thenReturn(integracao);

        var item = new IntegracaoFinanceiraSincronizacaoService.LancamentoExterno(
                "tx-1", "ENTRADA", new BigDecimal("25.00"), "Recebimento", sincronizadoEm.minusSeconds(60));
        var resultado = novoService().sincronizar(
                tenantId, usuarioId, integracaoId, List.of(item), "cursor-10", sincronizadoEm);

        ArgumentCaptor<List<ConciliacaoApplicationService.ImportacaoLancamento>> captor = ArgumentCaptor.forClass(List.class);
        verify(conciliacaoService).importarLote(eq(tenantId), eq(usuarioId), eq(contaId), captor.capture());
        assertEquals("PSP_TESTE", captor.getValue().get(0).origem());
        assertEquals("cursor-10", resultado.integracao().getCheckpoint());
        assertEquals(sincronizadoEm, resultado.integracao().getSincronizadoEm());
        verify(repository).save(integracao);
    }

    @Test
    void naoDeveAvancarCheckpointQuandoImportacaoFalhar() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID integracaoId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        IntegracaoFinanceira integracao = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), contaId, "BANCO_TESTE", null, usuarioId);
        when(repository.findByIdAndTenantId(integracaoId, tenantId)).thenReturn(Optional.of(integracao));
        when(conciliacaoService.importarLote(eq(tenantId), eq(usuarioId), eq(contaId), anyList()))
                .thenThrow(new IllegalArgumentException("falha de importacao"));

        var item = new IntegracaoFinanceiraSincronizacaoService.LancamentoExterno(
                "tx-2", "SAIDA", new BigDecimal("10.00"), "Tarifa", Instant.now());

        assertThrows(IllegalArgumentException.class, () -> novoService().sincronizar(
                tenantId, usuarioId, integracaoId, List.of(item), "cursor-nao-aplicar", Instant.now()));
        assertEquals(null, integracao.getCheckpoint());
        verify(repository, never()).save(any(IntegracaoFinanceira.class));
        verify(auditoria, never()).registrar(any(), any(), any(), any(), eq("SINCRONIZAR"), any(), any(), any());
    }

    private IntegracaoFinanceiraSincronizacaoService novoService() {
        return new IntegracaoFinanceiraSincronizacaoService(repository, conciliacaoService, auditoria);
    }
}
