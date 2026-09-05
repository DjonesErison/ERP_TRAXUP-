package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaReceberTest {
    @Test
    void deveReceberContaAbertaIntegralmente() {
        ContaReceber conta = novaConta();
        conta.receber();
        assertEquals("RECEBIDO", conta.getStatus());
        assertEquals(0, conta.getValorOriginal().compareTo(conta.getValorRecebido()));
        assertEquals(0, BigDecimal.ZERO.compareTo(conta.getSaldoAberto()));
    }

    @Test
    void devePermitirBaixasParciaisAteQuitarTitulo() {
        ContaReceber conta = novaConta();

        conta.receber(new BigDecimal("25.00"));
        assertEquals("PARCIAL", conta.getStatus());
        assertEquals(0, new BigDecimal("25.00").compareTo(conta.getValorRecebido()));
        assertEquals(0, new BigDecimal("75.00").compareTo(conta.getSaldoAberto()));

        conta.receber(new BigDecimal("75.00"));
        assertEquals("RECEBIDO", conta.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(conta.getSaldoAberto()));
    }

    @Test
    void naoDeveReceberValorMaiorQueSaldo() {
        ContaReceber conta = novaConta();
        conta.receber(new BigDecimal("30.00"));
        assertThrows(IllegalArgumentException.class,
                () -> conta.receber(new BigDecimal("70.01")));
    }

    @Test
    void naoDeveReceberDuasVezesDepoisDeQuitada() {
        ContaReceber conta = novaConta();
        conta.receber();
        assertThrows(IllegalArgumentException.class, conta::receber);
    }

    @Test
    void deveCancelarContaAberta() {
        ContaReceber conta = novaConta();
        conta.cancelar();
        assertEquals("CANCELADO", conta.getStatus());
    }

    @Test
    void naoDeveCancelarContaParcialmenteRecebida() {
        ContaReceber conta = novaConta();
        conta.receber(new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, conta::cancelar);
    }

    private ContaReceber novaConta() {
        return new ContaReceber(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "DOC-001", "Venda a prazo",
                new BigDecimal("100.00"), LocalDate.now().plusDays(30), UUID.randomUUID());
    }
}
