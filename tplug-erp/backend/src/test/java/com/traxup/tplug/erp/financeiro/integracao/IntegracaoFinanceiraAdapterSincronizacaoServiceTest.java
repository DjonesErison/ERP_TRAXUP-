package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class IntegracaoFinanceiraAdapterSincronizacaoServiceTest {

    @Test
    void deveResolverAdapterPorProvedorNormalizadoEUsarFluxoAtomico() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        IntegracaoFinanceira integracao = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "banco-x", "conta-externa", usuarioId);
        IntegracaoFinanceiraRepository repository = mock(IntegracaoFinanceiraRepository.class);
        IntegracaoFinanceiraSincronizacaoService sincronizacao = mock(IntegracaoFinanceiraSincronizacaoService.class);
        Instant sincronizadoEm = Instant.parse("2026-09-06T10:00:00Z");
        IntegracaoFinanceiraAdapter adapter = new IntegracaoFinanceiraAdapter() {
            @Override public String provedor() { return " BANCO-X "; }
            @Override public Resultado buscar(IntegracaoFinanceira recebida) {
                assertEquals(integracao, recebida);
                return new Resultado(List.of(), "cursor-2", sincronizadoEm);
            }
        };
        IntegracaoFinanceiraAdapterRegistry registry = new IntegracaoFinanceiraAdapterRegistry(List.of(adapter));
        when(repository.findByIdAndTenantId(integracao.getId(), tenantId)).thenReturn(Optional.of(integracao));
        when(sincronizacao.sincronizar(tenantId, usuarioId, integracao.getId(), List.of(), "cursor-2", sincronizadoEm))
                .thenReturn(new IntegracaoFinanceiraSincronizacaoService.ResultadoSincronizacao(integracao, List.of()));

        var service = new IntegracaoFinanceiraAdapterSincronizacaoService(repository, registry, sincronizacao);
        var resultado = service.sincronizar(tenantId, usuarioId, integracao.getId());

        assertEquals(integracao, resultado.integracao());
        verify(repository).findByIdAndTenantId(integracao.getId(), tenantId);
        verify(sincronizacao).sincronizar(tenantId, usuarioId, integracao.getId(), List.of(), "cursor-2", sincronizadoEm);
    }

    @Test
    void deveRejeitarIntegracaoDeOutroTenantAntesDeResolverAdapter() {
        UUID tenantId = UUID.randomUUID();
        UUID integracaoId = UUID.randomUUID();
        IntegracaoFinanceiraRepository repository = mock(IntegracaoFinanceiraRepository.class);
        IntegracaoFinanceiraSincronizacaoService sincronizacao = mock(IntegracaoFinanceiraSincronizacaoService.class);
        IntegracaoFinanceiraAdapterRegistry registry = mock(IntegracaoFinanceiraAdapterRegistry.class);
        when(repository.findByIdAndTenantId(integracaoId, tenantId)).thenReturn(Optional.empty());

        var service = new IntegracaoFinanceiraAdapterSincronizacaoService(repository, registry, sincronizacao);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.sincronizar(tenantId, UUID.randomUUID(), integracaoId));
        verifyNoInteractions(registry, sincronizacao);
    }

    @Test
    void deveRejeitarIntegracaoInativaAntesDeExecutarAdapter() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        IntegracaoFinanceira integracao = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "banco-x", null, usuarioId);
        integracao.desativar();
        IntegracaoFinanceiraRepository repository = mock(IntegracaoFinanceiraRepository.class);
        IntegracaoFinanceiraSincronizacaoService sincronizacao = mock(IntegracaoFinanceiraSincronizacaoService.class);
        IntegracaoFinanceiraAdapterRegistry registry = mock(IntegracaoFinanceiraAdapterRegistry.class);
        when(repository.findByIdAndTenantId(integracao.getId(), tenantId)).thenReturn(Optional.of(integracao));

        var service = new IntegracaoFinanceiraAdapterSincronizacaoService(repository, registry, sincronizacao);

        assertThrows(RecursoConflitanteException.class,
                () -> service.sincronizar(tenantId, usuarioId, integracao.getId()));
        verifyNoInteractions(registry, sincronizacao);
    }

    @Test
    void deveRejeitarProviderSemAdapterRegistrado() {
        IntegracaoFinanceiraAdapterRegistry registry = new IntegracaoFinanceiraAdapterRegistry(List.of());
        assertThrows(RuntimeException.class, () -> registry.obter("SEM-ADAPTER"));
    }

    @Test
    void deveRejeitarResultadoComMaisDeQuinhentosLancamentos() {
        var item = new IntegracaoFinanceiraSincronizacaoService.LancamentoExterno(
                "ref", "ENTRADA", java.math.BigDecimal.ONE, "teste", Instant.now());
        var itens = java.util.Collections.nCopies(501, item);
        assertThrows(IllegalArgumentException.class,
                () -> new IntegracaoFinanceiraAdapter.Resultado(itens, "cursor", Instant.now()));
    }
}
