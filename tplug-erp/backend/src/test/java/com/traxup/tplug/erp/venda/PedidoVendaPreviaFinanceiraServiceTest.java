package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.financeiro.pagamento.AjusteComercialTipo;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaPreviaFinanceiraServiceTest {
    @Mock PedidoVendaRepository pedidoRepository;
    @Mock PedidoVendaItemRepository itemRepository;
    @Mock CondicaoPagamentoRepository condicaoRepository;
    @Mock CondicaoPagamentoParcelaRepository parcelaRepository;

    @Test
    void devePreverDescontoJurosEntradaESaldoParcelado() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID condicaoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getCondicaoPagamentoId()).thenReturn(condicaoId);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        PedidoVendaItem item = mock(PedidoVendaItem.class);
        when(item.getTotalItem()).thenReturn(new BigDecimal("1000.0000"));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of(item));

        CondicaoPagamento condicao = new CondicaoPagamento(tenantId, "10X", "10x",
                AjusteComercialTipo.PERCENTUAL, new BigDecimal("5.0000"),
                AjusteComercialTipo.PERCENTUAL, new BigDecimal("10.0000"),
                AjusteComercialTipo.VALOR_FIXO, new BigDecimal("200.0000"));
        when(condicaoRepository.findByIdAndTenantId(condicaoId, tenantId)).thenReturn(Optional.of(condicao));
        when(parcelaRepository.findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(tenantId, condicaoId))
                .thenReturn(List.of(
                        new CondicaoPagamentoParcela(tenantId, condicaoId, 1, 30, new BigDecimal("50.0000")),
                        new CondicaoPagamentoParcela(tenantId, condicaoId, 2, 60, new BigDecimal("50.0000"))));

        PedidoVendaPreviaFinanceiraService.PreviaFinanceira previa = service().prever(tenantId, pedidoId);

        assertEquals(new BigDecimal("1000.0000"), previa.totalLiquido());
        assertEquals(new BigDecimal("100.0000"), previa.desconto());
        assertEquals(new BigDecimal("45.0000"), previa.juros());
        assertEquals(new BigDecimal("200.0000"), previa.entrada());
        assertEquals(new BigDecimal("945.0000"), previa.totalFinanceiro());
        assertEquals(new BigDecimal("745.0000"), previa.saldoParcelar());
        assertEquals(3, previa.titulos().size());
        assertEquals("ENTRADA", previa.titulos().get(0).tipo());
        assertEquals(new BigDecimal("372.5000"), previa.titulos().get(1).valor());
        assertEquals(LocalDate.now().plusDays(60), previa.titulos().get(2).vencimento());
    }

    @Test
    void deveGerarUmaParcelaSemCondicaoConfigurada() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        PedidoVendaItem item = mock(PedidoVendaItem.class);
        when(item.getTotalItem()).thenReturn(new BigDecimal("125.50"));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of(item));

        PedidoVendaPreviaFinanceiraService.PreviaFinanceira previa = service().prever(tenantId, pedidoId);

        assertEquals(new BigDecimal("125.5000"), previa.totalFinanceiro());
        assertEquals(1, previa.titulos().size());
        assertEquals(LocalDate.now(), previa.titulos().get(0).vencimento());
    }

    @Test
    void deveRespeitarTenantNaBuscaDoPedido() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service().prever(tenantId, pedidoId));
        verify(pedidoRepository).findByIdAndTenantId(pedidoId, tenantId);
    }

    private PedidoVendaPreviaFinanceiraService service() {
        return new PedidoVendaPreviaFinanceiraService(pedidoRepository, itemRepository, condicaoRepository, parcelaRepository);
    }
}
