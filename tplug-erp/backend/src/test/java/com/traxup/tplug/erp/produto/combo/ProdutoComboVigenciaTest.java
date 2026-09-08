package com.traxup.tplug.erp.produto.combo;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoComboVigenciaTest {

    @Test
    void deveConsiderarLimitesDaVigenciaInclusivos() {
        Instant inicio = Instant.parse("2026-09-08T10:00:00Z");
        Instant fim = inicio.plus(2, ChronoUnit.HOURS);
        ProdutoComboVigencia vigencia = new ProdutoComboVigencia(
                UUID.randomUUID(), UUID.randomUUID(), inicio, fim);

        assertThat(vigencia.vigenteEm(inicio)).isTrue();
        assertThat(vigencia.vigenteEm(fim)).isTrue();
        assertThat(vigencia.vigenteEm(inicio.minusMillis(1))).isFalse();
        assertThat(vigencia.vigenteEm(fim.plusMillis(1))).isFalse();
    }

    @Test
    void deveAceitarLimitesAbertos() {
        Instant agora = Instant.parse("2026-09-08T10:00:00Z");
        ProdutoComboVigencia semInicio = new ProdutoComboVigencia(
                UUID.randomUUID(), UUID.randomUUID(), null, agora);
        ProdutoComboVigencia semFim = new ProdutoComboVigencia(
                UUID.randomUUID(), UUID.randomUUID(), agora, null);

        assertThat(semInicio.vigenteEm(agora.minus(1, ChronoUnit.HOURS))).isTrue();
        assertThat(semFim.vigenteEm(agora.plus(1, ChronoUnit.HOURS))).isTrue();
    }

    @Test
    void deveRejeitarIntervaloInvertido() {
        Instant inicio = Instant.parse("2026-09-08T12:00:00Z");
        Instant fim = Instant.parse("2026-09-08T10:00:00Z");

        assertThatThrownBy(() -> new ProdutoComboVigencia(
                UUID.randomUUID(), UUID.randomUUID(), inicio, fim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inicio da vigencia nao pode ser posterior ao fim");
    }
}
