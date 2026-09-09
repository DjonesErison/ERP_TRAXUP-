package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.venda.PedidoVendaItem;
import com.traxup.tplug.erp.venda.PedidoVendaItemApplicationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PdvVendaItemSincronizacaoApplicationServiceTest {

    private final PdvVendaSincronizacaoRepository sincronizacaoRepository = mock(PdvVendaSincronizacaoRepository.class);
    private final PedidoVendaItemApplicationService itemService = mock(PedidoVendaItemApplicationService.class);
    private final PdvVendaItemSincronizacaoApplicationService service =
            new PdvVendaItemSincronizacaoApplicationService(sincronizacaoRepository, itemService);

    @Test
    void deveRejeitarSincronizacaoDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        PdvVendaSincronizacao sync = novaSincronizacao(UUID.randomUUID());
        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));

        assertThatThrownBy(() -> service.sincronizar(
                tenantId, UUID.randomUUID(), sync.getId(), UUID.randomUUID(), UUID.randomUUID(), null,
                BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("tenant");
    }

    @Test
    void deveExigirPedidoVinculadoAntesDeSincronizarItem() {
        UUID tenantId = UUID.randomUUID();
        PdvVendaSincronizacao sync = novaSincronizacao(tenantId);
        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));

        assertThatThrownBy(() -> service.sincronizar(
                tenantId, UUID.randomUUID(), sync.getId(), UUID.randomUUID(), UUID.randomUUID(), null,
                BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedido de venda vinculado");
    }

    @Test
    void deveDelegarAoNucleoDeVendasComIdentidadeLocal() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID itemLocalId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PdvVendaSincronizacao sync = novaSincronizacao(tenantId);
        sync.vincularPedidoVenda(pedidoId);
        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));

        PedidoVendaItem item = new PedidoVendaItem(
                tenantId, pedidoId, produtoId, null, BigDecimal.ONE, BigDecimal.TEN,
                sync.getId(), itemLocalId);
        var esperado = new PedidoVendaItemApplicationService.ResultadoPdv(item, false);
        when(itemService.adicionarPdvIdempotente(
                tenantId, usuarioId, pedidoId, sync.getId(), itemLocalId, produtoId, null,
                BigDecimal.ONE, BigDecimal.TEN)).thenReturn(esperado);

        var resultado = service.sincronizar(
                tenantId, usuarioId, sync.getId(), itemLocalId, produtoId, null,
                BigDecimal.ONE, BigDecimal.TEN);

        assertThat(resultado).isSameAs(esperado);
        verify(itemService).adicionarPdvIdempotente(
                tenantId, usuarioId, pedidoId, sync.getId(), itemLocalId, produtoId, null,
                BigDecimal.ONE, BigDecimal.TEN);
    }

    private PdvVendaSincronizacao novaSincronizacao(UUID tenantId) {
        return new PdvVendaSincronizacao(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, 10L, "a".repeat(64), Instant.parse("2026-09-08T20:00:00Z"));
    }
}
