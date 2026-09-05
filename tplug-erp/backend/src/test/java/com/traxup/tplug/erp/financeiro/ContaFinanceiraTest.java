package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContaFinanceiraTest {
    @Test
    void deveRegistrarEntradaESaidaSemFicarNegativo() {
        ContaFinanceira conta = novaConta();
        conta.movimentar("ENTRADA", new BigDecimal("100.00"));
        conta.movimentar("SAIDA", new BigDecimal("40.00"));
        assertEquals(0, conta.getSaldo().compareTo(new BigDecimal("60.00")));
    }

    @Test
    void naoDevePermitirSaidaMaiorQueSaldo() {
        ContaFinanceira conta = novaConta();
        conta.movimentar("ENTRADA", new BigDecimal("50.00"));
        assertThrows(IllegalArgumentException.class,
                () -> conta.movimentar("SAIDA", new BigDecimal("50.01")));
        assertEquals(0, conta.getSaldo().compareTo(new BigDecimal("50.00")));
    }

    @Test
    void contaInativaNaoPodeSerMovimentada() {
        ContaFinanceira conta = novaConta();
        conta.desativar();
        assertThrows(IllegalArgumentException.class,
                () -> conta.movimentar("ENTRADA", BigDecimal.ONE));
    }

    private ContaFinanceira novaConta() {
        return new ContaFinanceira(UUID.randomUUID(), UUID.randomUUID(), "Caixa principal", "CAIXA", UUID.randomUUID());
    }
}
