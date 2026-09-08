package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.inventario.api.InventarioContagemResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InventarioContagemCegaTest {
    @Test
    void sessaoCegaAbertaDeveOcultarSaldoEDivergencia() {
        InventarioSessao sessao = new InventarioSessao(UUID.randomUUID(), UUID.randomUUID(), "Contagem", UUID.randomUUID(), true);
        InventarioContagem contagem = new InventarioContagem(UUID.randomUUID(), sessao.getId(), "PRODUTO", UUID.randomUUID(),
                new BigDecimal("10.0000"), new BigDecimal("8.0000"), UUID.randomUUID());

        InventarioContagemResponse response = InventarioContagemResponse.from(contagem, sessao.deveOcultarSaldoDuranteContagem());

        assertThat(sessao.isContagemCega()).isTrue();
        assertThat(response.quantidadeSistema()).isNull();
        assertThat(response.divergencia()).isNull();
        assertThat(response.quantidadeContada()).isEqualByComparingTo("8.0000");
    }

    @Test
    void conclusaoDeveLiberarConferenciaDaContagemCega() {
        InventarioSessao sessao = new InventarioSessao(UUID.randomUUID(), UUID.randomUUID(), "Contagem", UUID.randomUUID(), true);
        sessao.concluir(UUID.randomUUID());
        InventarioContagem contagem = new InventarioContagem(UUID.randomUUID(), sessao.getId(), "PRODUTO", UUID.randomUUID(),
                new BigDecimal("10.0000"), new BigDecimal("8.0000"), UUID.randomUUID());

        InventarioContagemResponse response = InventarioContagemResponse.from(contagem, sessao.deveOcultarSaldoDuranteContagem());

        assertThat(response.quantidadeSistema()).isEqualByComparingTo("10.0000");
        assertThat(response.divergencia()).isEqualByComparingTo("-2.0000");
    }

    @Test
    void sessaoConvencionalPermaneceCompativel() {
        InventarioSessao sessao = new InventarioSessao(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID());
        assertThat(sessao.isContagemCega()).isFalse();
        assertThat(sessao.deveOcultarSaldoDuranteContagem()).isFalse();
    }
}
