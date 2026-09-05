package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaReceberTest {
    @Test
    void deveReceberContaAberta() {
        ContaReceber conta = novaConta();
        conta.receber();
        assertEquals("RECEBIDO", conta.getStatus());
        assertEquals(0, conta.getValorOriginal().compareTo(conta.getValorRecebido()));
    }

    @Test
    void naoDeveReceberDuasVezes() {
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
    void naoDeveCancelarContaRecebida() {
        ContaReceber conta = novaConta();
        conta.receber();
        assertThrows(IllegalArgumentException.class, conta::cancelar);
    }

    private ContaReceber novaConta() {
        return new ContaReceber(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "DOC-001", "Venda a prazo",
                new BigDecimal("100.00"), LocalDate.now().plusDays(30), UUID.randomUUID());
    }
}
