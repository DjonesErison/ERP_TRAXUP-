package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PdvVendaFechamentoApplicationServiceTest {
    private PdvVendaSincronizacaoRepository sincronizacaoRepository;
    private PedidoVendaRepository pedidoVendaRepository;
    private PedidoVendaApplicationService pedidoVendaService;
    private PdvVendaFechamentoApplicationService service;

    @BeforeEach
    void setUp() {
        sincronizacaoRepository = mock(PdvVendaSincronizacaoRepository.class);
        pedidoVendaRepository = mock(PedidoVendaRepository.class);
        pedidoVendaService = mock(PedidoVendaApplicationService.class);
        service = new PdvVendaFechamentoApplicationService(sincronizacaoRepository, pedidoVendaRepository, pedidoVendaService);
    }

    @Test
    void deveConfigurarAbrirEFaturarRascunho() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID formaId = UUID.randomUUID();
        UUID condicaoId = UUID.randomUUID();
        PdvVendaSincronizacao sync = syncVinculada(tenantId);
        PedidoVenda pedido = novoPedido(tenantId, sync.getFilialId());

        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));
        when(pedidoVendaRepository.buscarParaAtualizar(pedido.getId(), tenantId)).thenReturn(Optional.of(pedido));
        when(pedidoVendaService.configurarPagamento(tenantId, usuarioId, pedido.getId(), formaId, condicaoId))
                .thenAnswer(inv -> { pedido.configurarPagamento(formaId, condicaoId); return pedido; });
        when(pedidoVendaService.abrir(tenantId, usuarioId, pedido.getId()))
                .thenAnswer(inv -> { pedido.abrir(); return pedido; });
        when(pedidoVendaService.faturar(tenantId, usuarioId, pedido.getId()))
                .thenAnswer(inv -> { pedido.faturar(); return pedido; });

        var resultado = service.fechar(tenantId, usuarioId, sync.getId(), formaId, condicaoId);

        assertThat(resultado.repetida()).isFalse();
        assertThat(resultado.pedidoVenda().getStatus()).isEqualTo("FATURADO");
        verify(pedidoVendaService).configurarPagamento(tenantId, usuarioId, pedido.getId(), formaId, condicaoId);
        verify(pedidoVendaService).abrir(tenantId, usuarioId, pedido.getId());
        verify(pedidoVendaService).faturar(tenantId, usuarioId, pedido.getId());
    }

    @Test
    void replayDoMesmoFechamentoDeveRetornarFaturadoSemReprocessar() {
        UUID tenantId = UUID.randomUUID();
        UUID formaId = UUID.randomUUID();
        UUID condicaoId = UUID.randomUUID();
        PdvVendaSincronizacao sync = syncVinculada(tenantId);
        PedidoVenda pedido = novoPedido(tenantId, sync.getFilialId());
        pedido.configurarPagamento(formaId, condicaoId);
        pedido.abrir();
        pedido.faturar();

        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));
        when(pedidoVendaRepository.buscarParaAtualizar(pedido.getId(), tenantId)).thenReturn(Optional.of(pedido));

        var resultado = service.fechar(tenantId, UUID.randomUUID(), sync.getId(), formaId, condicaoId);

        assertThat(resultado.repetida()).isTrue();
        assertThat(resultado.pedidoVenda().getId()).isEqualTo(pedido.getId());
        verifyNoInteractions(pedidoVendaService);
    }

    @Test
    void replayComPagamentoDiferenteDeveSerRejeitado() {
        UUID tenantId = UUID.randomUUID();
        UUID formaId = UUID.randomUUID();
        UUID condicaoId = UUID.randomUUID();
        PdvVendaSincronizacao sync = syncVinculada(tenantId);
        PedidoVenda pedido = novoPedido(tenantId, sync.getFilialId());
        pedido.configurarPagamento(formaId, condicaoId);
        pedido.abrir();
        pedido.faturar();

        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));
        when(pedidoVendaRepository.buscarParaAtualizar(pedido.getId(), tenantId)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.fechar(tenantId, UUID.randomUUID(), sync.getId(), UUID.randomUUID(), condicaoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configuracao de pagamento diferente");
        verifyNoInteractions(pedidoVendaService);
    }

    @Test
    void sincronizacaoDeOutroTenantDeveSerInvisivel() {
        UUID tenantId = UUID.randomUUID();
        PdvVendaSincronizacao sync = syncVinculada(UUID.randomUUID());
        when(sincronizacaoRepository.findById(sync.getId())).thenReturn(Optional.of(sync));

        assertThatThrownBy(() -> service.fechar(tenantId, UUID.randomUUID(), sync.getId(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(pedidoVendaRepository, pedidoVendaService);
    }

    private PdvVendaSincronizacao syncVinculada(UUID tenantId) {
        PdvVendaSincronizacao sync = new PdvVendaSincronizacao(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, 1L, "a".repeat(64), Instant.parse("2026-09-08T20:00:00Z"));
        PedidoVenda pedido = novoPedido(tenantId, sync.getFilialId());
        sync.vincularPedidoVenda(pedido.getId());
        return sync;
    }

    private PedidoVenda novoPedido(UUID tenantId, UUID filialId) {
        return new PedidoVenda(tenantId, filialId, null, "PDV-" + UUID.randomUUID(), null, UUID.randomUUID());
    }
}
