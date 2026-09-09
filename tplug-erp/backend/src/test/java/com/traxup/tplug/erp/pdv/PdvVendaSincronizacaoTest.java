package com.traxup.tplug.erp.pdv;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PdvVendaSincronizacaoTest {
    @Test
    void mesmaOperacaoDeveCorresponderAoMesmoNumeroEChecksum() {
        String checksum = "a".repeat(64);
        PdvVendaSincronizacao sync = new PdvVendaSincronizacao(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, 10L, checksum, Instant.parse("2026-09-08T20:00:00Z"));

        assertThat(sync.corresponde(10L, checksum)).isTrue();
        assertThat(sync.corresponde(11L, checksum)).isFalse();
        assertThat(sync.corresponde(10L, "b".repeat(64))).isFalse();
    }

    @Test
    void deveMarcarOperacaoComoProcessadaUmaUnicaVezParaMesmoPedido() {
        PdvVendaSincronizacao sync = new PdvVendaSincronizacao(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                2, 42L, "c".repeat(64), Instant.parse("2026-09-08T20:00:00Z"));
        UUID pedidoId = UUID.randomUUID();

        sync.marcarProcessada(pedidoId);
        Instant processadoEm = sync.getProcessadoEm();
        sync.marcarProcessada(pedidoId);

        assertThat(sync.isProcessada()).isTrue();
        assertThat(sync.getPedidoVendaId()).isEqualTo(pedidoId);
        assertThat(sync.getProcessadoEm()).isEqualTo(processadoEm);
    }
}
