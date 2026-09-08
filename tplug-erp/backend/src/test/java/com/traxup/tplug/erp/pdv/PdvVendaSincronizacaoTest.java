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
}
