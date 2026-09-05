package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaPagarStatusTest {
    @Test
    void deveIniciarAberta() {
        ContaPagar conta = novaConta();
        assertEquals("ABERTO", conta.getStatus());
        assertEquals(0, conta.getValorPago().compareTo(BigDecimal.ZERO));
    }

    @Test
    void devePagarIntegralmenteContaAberta() {
        ContaPagar conta = novaConta();
        conta.pagar();
        assertEquals("PAGO", conta.getStatus());
        assertEquals(0, conta.getValorPago().compareTo(conta.getValorOriginal()));
    }

    @Test
    void naoDevePagarDuasVezes() {
        ContaPagar conta = novaConta();
        conta.pagar();
        assertThrows(IllegalArgumentException.class, conta::pagar);
    }

    @Test
    void deveCancelarSomenteContaAberta() {
        ContaPagar conta = novaConta();
        conta.cancelar();
        assertEquals("CANCELADO", conta.getStatus());
        assertThrows(IllegalArgumentException.class, conta::cancelar);
    }

    private ContaPagar novaConta() {
        return new ContaPagar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CP-001", "Fornecedor",
                new BigDecimal("100.0000"), LocalDate.now().plusDays(10), UUID.randomUUID());
    }
}
