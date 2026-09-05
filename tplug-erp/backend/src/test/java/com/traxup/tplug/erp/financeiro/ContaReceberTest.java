package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaReceberTest {
    @Test
    void deveRegistrarRecebimentoParcial() {
        ContaReceber conta = novaConta();

        conta.registrarRecebimento(new BigDecimal("40.00"));

        assertEquals("PARCIALMENTE_RECEBIDO", conta.getStatus());
        assertEquals(0, new BigDecimal("40.00").compareTo(conta.getValorRecebido()));
        assertEquals(0, new BigDecimal("60.00").compareTo(conta.saldoAberto()));
    }

    @Test
    void deveQuitarContaComMaisDeUmRecebimento() {
        ContaReceber conta = novaConta();

        conta.registrarRecebimento(new BigDecimal("40.00"));
        conta.registrarRecebimento(new BigDecimal("60.00"));

        assertEquals("RECEBIDO", conta.getStatus());
        assertEquals(0, conta.getValorOriginal().compareTo(conta.getValorRecebido()));
        assertEquals(0, BigDecimal.ZERO.compareTo(conta.saldoAberto()));
    }

    @Test
    void deveReceberIntegralmentePeloEndpointLegado() {
        ContaReceber conta = novaConta();

        conta.receber();

        assertEquals("RECEBIDO", conta.getStatus());
        assertEquals(0, conta.getValorOriginal().compareTo(conta.getValorRecebido()));
    }

    @Test
    void naoDeveReceberAcimaDoSaldo() {
        ContaReceber conta = novaConta();

        assertThrows(RegraNegocioException.class,
                () -> conta.registrarRecebimento(new BigDecimal("100.01")));
    }

    @Test
    void naoDeveReceberValorNuloZeroOuNegativo() {
        ContaReceber conta = novaConta();

        assertThrows(RegraNegocioException.class, () -> conta.registrarRecebimento(null));
        assertThrows(RegraNegocioException.class, () -> conta.registrarRecebimento(BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class,
                () -> conta.registrarRecebimento(new BigDecimal("-1.00")));
    }

    @Test
    void naoDeveCancelarContaComRecebimentoParcial() {
        ContaReceber conta = novaConta();
        conta.registrarRecebimento(new BigDecimal("10.00"));

        assertThrows(RegraNegocioException.class, conta::cancelar);
    }

    private ContaReceber novaConta() {
        return new ContaReceber(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "DOC-001", "Venda a prazo",
                new BigDecimal("100.00"), LocalDate.now().plusDays(30), UUID.randomUUID());
    }
}
