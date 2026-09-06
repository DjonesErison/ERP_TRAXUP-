package com.traxup.tplug.erp.financeiro.pagamento;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AjusteComercialCalculadoraTest {

    @Test
    void deveAplicarDescontoDepoisJurosEEntradaSobreTotalFinanceiro() {
        CondicaoPagamento condicao = new CondicaoPagamento(UUID.randomUUID(), "10X", "Parcelado",
                AjusteComercialTipo.PERCENTUAL, new BigDecimal("5"),
                AjusteComercialTipo.PERCENTUAL, new BigDecimal("10"),
                AjusteComercialTipo.VALOR_FIXO, new BigDecimal("200"));

        AjusteComercialCalculadora.Resultado resultado = AjusteComercialCalculadora.calcular(new BigDecimal("1000"), condicao);

        assertEquals(new BigDecimal("100.0000"), resultado.desconto());
        assertEquals(new BigDecimal("45.0000"), resultado.juros());
        assertEquals(new BigDecimal("945.0000"), resultado.totalFinanceiro());
        assertEquals(new BigDecimal("200.0000"), resultado.entrada());
        assertEquals(new BigDecimal("745.0000"), resultado.saldoParcelar());
    }

    @Test
    void deveManterVendaSemAcrescimoQuandoCondicaoNaoConfiguraAjustes() {
        CondicaoPagamento condicao = new CondicaoPagamento(UUID.randomUUID(), "PADRAO", "Padrao");
        AjusteComercialCalculadora.Resultado resultado = AjusteComercialCalculadora.calcular(new BigDecimal("1000"), condicao);
        assertEquals(new BigDecimal("1000.0000"), resultado.totalFinanceiro());
        assertEquals(new BigDecimal("1000.0000"), resultado.saldoParcelar());
    }

    @Test
    void deveBloquearDescontoFixoMaiorQueVenda() {
        CondicaoPagamento condicao = new CondicaoPagamento(UUID.randomUUID(), "DESC", "Desconto",
                null, null, AjusteComercialTipo.VALOR_FIXO, new BigDecimal("1001"), null, null);
        assertThrows(IllegalArgumentException.class,
                () -> AjusteComercialCalculadora.calcular(new BigDecimal("1000"), condicao));
    }

    @Test
    void deveBloquearEntradaFixaMaiorQueTotalFinanceiro() {
        CondicaoPagamento condicao = new CondicaoPagamento(UUID.randomUUID(), "ENT", "Entrada",
                null, null, null, null, AjusteComercialTipo.VALOR_FIXO, new BigDecimal("1001"));
        assertThrows(IllegalArgumentException.class,
                () -> AjusteComercialCalculadora.calcular(new BigDecimal("1000"), condicao));
    }
}
