package com.traxup.tplug.erp.pdv;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PdvCaixaSessaoTest {
    @Test void deveCalcularDiferencaAoFechar() {
        PdvCaixaSessao s = new PdvCaixaSessao(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"));
        s.fechar(UUID.randomUUID(), new BigDecimal("250.00"), new BigDecimal("247.50"), "conferencia");
        assertThat(s.getStatus()).isEqualTo("FECHADO");
        assertThat(s.getDiferencaFechamento()).isEqualByComparingTo("-2.50");
        assertThat(s.getFechadoEm()).isNotNull();
    }

    @Test void naoDeveFecharDuasVezes() {
        PdvCaixaSessao s = new PdvCaixaSessao(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        s.fechar(UUID.randomUUID(), BigDecimal.ZERO, BigDecimal.ZERO, null);
        assertThatThrownBy(() -> s.fechar(UUID.randomUUID(), BigDecimal.ZERO, BigDecimal.ZERO, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nao esta aberta");
    }
}
