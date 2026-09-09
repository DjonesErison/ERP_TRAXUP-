package com.traxup.tplug.erp.pdv;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdvVendaSincronizacaoTest {
    @Test
    void mesmaOperacaoDeveCorresponderAoMesmoNumeroEChecksum() {
        String checksum = "a".repeat(64);
        PdvVendaSincronizacao sync = novaSincronizacao(checksum);

        assertThat(sync.corresponde(10L, checksum)).isTrue();
        assertThat(sync.corresponde(11L, checksum)).isFalse();
        assertThat(sync.corresponde(10L, "b".repeat(64))).isFalse();
    }

    @Test
    void devePermitirRepetirOMesmoVinculoSemTrocarOPedido() {
        PdvVendaSincronizacao sync = novaSincronizacao("a".repeat(64));
        UUID pedidoId = UUID.randomUUID();

        sync.vincularPedidoVenda(pedidoId);
        Instant vinculadoEm = sync.getPedidoVendaVinculadoEm();
        sync.vincularPedidoVenda(pedidoId);

        assertThat(sync.possuiPedidoVenda()).isTrue();
        assertThat(sync.getPedidoVendaId()).isEqualTo(pedidoId);
        assertThat(sync.getPedidoVendaVinculadoEm()).isEqualTo(vinculadoEm);
    }

    @Test
    void deveRejeitarTrocaDoPedidoDepoisDoVinculo() {
        PdvVendaSincronizacao sync = novaSincronizacao("a".repeat(64));
        sync.vincularPedidoVenda(UUID.randomUUID());

        assertThatThrownBy(() -> sync.vincularPedidoVenda(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("outro pedido");
    }

    private PdvVendaSincronizacao novaSincronizacao(String checksum) {
        return new PdvVendaSincronizacao(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, 10L, checksum, Instant.parse("2026-09-08T20:00:00Z"));
    }
}
