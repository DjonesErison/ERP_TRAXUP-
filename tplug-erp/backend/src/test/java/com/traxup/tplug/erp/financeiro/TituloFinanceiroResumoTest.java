package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TituloFinanceiroResumoTest {

    @Test
    void deveResumirContasReceberSemTratarCanceladaComoSaldoAtivo() {
        ContaReceber aberta = novaContaReceber("100.0000");
        ContaReceber parcial = novaContaReceber("100.0000");
        parcial.receber(new BigDecimal("40.0000"));
        ContaReceber recebida = novaContaReceber("50.0000");
        recebida.receber();
        ContaReceber cancelada = novaContaReceber("30.0000");
        cancelada.cancelar();

        TituloFinanceiroResumo resumo = TituloFinanceiroResumo.deContasReceber(
                List.of(aberta, parcial, recebida, cancelada));

        assertEquals(4, resumo.quantidade());
        assertValor("280.0000", resumo.valorOriginalTotal());
        assertValor("90.0000", resumo.valorLiquidadoTotal());
        assertValor("160.0000", resumo.saldoAtivoTotal());
        assertEquals(1, resumo.abertos());
        assertEquals(1, resumo.parciais());
        assertEquals(1, resumo.liquidados());
        assertEquals(1, resumo.cancelados());
    }

    @Test
    void deveResumirContasPagarComPagamentosParciaisEIntegrais() {
        ContaPagar aberta = novaContaPagar("80.0000");
        ContaPagar parcial = novaContaPagar("100.0000");
        parcial.pagar(new BigDecimal("25.0000"));
        ContaPagar paga = novaContaPagar("40.0000");
        paga.pagar();
        ContaPagar cancelada = novaContaPagar("20.0000");
        cancelada.cancelar();

        TituloFinanceiroResumo resumo = TituloFinanceiroResumo.deContasPagar(
                List.of(aberta, parcial, paga, cancelada));

        assertEquals(4, resumo.quantidade());
        assertValor("240.0000", resumo.valorOriginalTotal());
        assertValor("65.0000", resumo.valorLiquidadoTotal());
        assertValor("155.0000", resumo.saldoAtivoTotal());
        assertEquals(1, resumo.abertos());
        assertEquals(1, resumo.parciais());
        assertEquals(1, resumo.liquidados());
        assertEquals(1, resumo.cancelados());
    }

    @Test
    void deveRetornarZerosQuandoNaoHouverTitulos() {
        TituloFinanceiroResumo resumo = TituloFinanceiroResumo.deContasReceber(List.of());

        assertEquals(0, resumo.quantidade());
        assertValor("0", resumo.valorOriginalTotal());
        assertValor("0", resumo.valorLiquidadoTotal());
        assertValor("0", resumo.saldoAtivoTotal());
        assertEquals(0, resumo.abertos());
        assertEquals(0, resumo.parciais());
        assertEquals(0, resumo.liquidados());
        assertEquals(0, resumo.cancelados());
    }

    private ContaReceber novaContaReceber(String valor) {
        return new ContaReceber(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "CR-001", "Cliente", new BigDecimal(valor), LocalDate.now().plusDays(10), UUID.randomUUID());
    }

    private ContaPagar novaContaPagar(String valor) {
        return new ContaPagar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "CP-001", "Fornecedor", new BigDecimal(valor), LocalDate.now().plusDays(10), UUID.randomUUID());
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, atual.compareTo(new BigDecimal(esperado)));
    }
}
