package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.financeiro.pagamento.AjusteComercialTipo;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoVendaPlanoFinanceiroCalculadoraTest {

    @Test
    void deveGerarMesmoPlanoParaEntradaEParcelas() {
        UUID tenantId = UUID.randomUUID();
        UUID condicaoId = UUID.randomUUID();
        LocalDate dataBase = LocalDate.of(2026, 9, 6);
        CondicaoPagamento condicao = new CondicaoPagamento(tenantId, "2X", "2x",
                AjusteComercialTipo.PERCENTUAL, new BigDecimal("5.0000"),
                AjusteComercialTipo.PERCENTUAL, new BigDecimal("10.0000"),
                AjusteComercialTipo.VALOR_FIXO, new BigDecimal("200.0000"));
        List<CondicaoPagamentoParcela> parcelas = List.of(
                new CondicaoPagamentoParcela(tenantId, condicaoId, 1, 30, new BigDecimal("50.0000")),
                new CondicaoPagamentoParcela(tenantId, condicaoId, 2, 60, new BigDecimal("50.0000")));

        PedidoVendaPlanoFinanceiroCalculadora.Plano plano = PedidoVendaPlanoFinanceiroCalculadora.calcular(
                new BigDecimal("1000.0000"), condicao, parcelas, dataBase);

        assertEquals(new BigDecimal("945.0000"), plano.totalFinanceiro());
        assertEquals(new BigDecimal("745.0000"), plano.saldoParcelar());
        assertEquals(3, plano.titulos().size());
        assertEquals("ENTRADA", plano.titulos().get(0).tipo());
        assertEquals(new BigDecimal("200.0000"), plano.titulos().get(0).valor());
        assertEquals(new BigDecimal("372.5000"), plano.titulos().get(1).valor());
        assertEquals(dataBase.plusDays(60), plano.titulos().get(2).vencimento());
    }

    @Test
    void devePreservarCompatibilidadeSemCondicao() {
        LocalDate dataBase = LocalDate.of(2026, 9, 6);

        PedidoVendaPlanoFinanceiroCalculadora.Plano plano = PedidoVendaPlanoFinanceiroCalculadora.calcular(
                new BigDecimal("125.50"), null, List.of(), dataBase);

        assertEquals(new BigDecimal("125.5000"), plano.totalFinanceiro());
        assertEquals(1, plano.titulos().size());
        assertEquals("PARCELA", plano.titulos().get(0).tipo());
        assertEquals(dataBase, plano.titulos().get(0).vencimento());
    }

    @Test
    void deveExigirParcelasQuandoExisteSaldoAParcelar() {
        CondicaoPagamento condicao = new CondicaoPagamento(UUID.randomUUID(), "30D", "30 dias");

        assertThrows(IllegalArgumentException.class, () -> PedidoVendaPlanoFinanceiroCalculadora.calcular(
                new BigDecimal("100.0000"), condicao, List.of(), LocalDate.of(2026, 9, 6)));
    }
}
