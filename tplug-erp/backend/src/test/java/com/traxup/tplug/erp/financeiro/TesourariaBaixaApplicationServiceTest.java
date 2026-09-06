package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TesourariaBaixaApplicationServiceTest {
    @Mock ContaReceberApplicationService contaReceberService;
    @Mock ContaPagarApplicationService contaPagarService;
    @Mock ContaFinanceiraApplicationService contaFinanceiraService;

    @Test
    void deveRegistrarRecebimentoEEntradaComOrigemIdempotente() {
        UUID tenantId = UUID.randomUUID(); UUID usuarioId = UUID.randomUUID(); UUID filialId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID(); UUID contaFinanceiraId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("50.00"); String chave = "recebimento-001";
        ContaReceber titulo = new ContaReceber(tenantId, filialId, UUID.randomUUID(), "R-1", "Receber",
                new BigDecimal("100.00"), LocalDate.now().plusDays(10), usuarioId);
        ContaFinanceira contaFinanceira = new ContaFinanceira(tenantId, filialId, "Caixa", "CAIXA", usuarioId);
        when(contaFinanceiraService.existeMovimentoPorOrigem(tenantId, "RECEBIMENTO_CONTA_RECEBER", tituloId, chave)).thenReturn(false);
        when(contaReceberService.buscar(tenantId, tituloId)).thenReturn(titulo);
        when(contaFinanceiraService.buscar(tenantId, contaFinanceiraId)).thenReturn(contaFinanceira);
        when(contaReceberService.receber(tenantId, usuarioId, tituloId, valor)).thenReturn(titulo);

        service().receberEmConta(tenantId, usuarioId, tituloId, contaFinanceiraId, valor, chave);

        verify(contaReceberService).receber(tenantId, usuarioId, tituloId, valor);
        verify(contaFinanceiraService).movimentarComOrigem(
                tenantId, usuarioId, contaFinanceiraId, "ENTRADA", valor,
                "RECEBIMENTO_CONTA_RECEBER:" + tituloId, "RECEBIMENTO_CONTA_RECEBER", tituloId, chave);
    }

    @Test
    void deveBloquearRetryAntesDeNovaBaixa() {
        UUID tenantId = UUID.randomUUID(); UUID tituloId = UUID.randomUUID(); String chave = "retry-123";
        when(contaFinanceiraService.existeMovimentoPorOrigem(
                tenantId, "RECEBIMENTO_CONTA_RECEBER", tituloId, chave)).thenReturn(true);

        assertThrows(RecursoConflitanteException.class, () -> service().receberEmConta(
                tenantId, UUID.randomUUID(), tituloId, UUID.randomUUID(), new BigDecimal("20.00"), chave));

        verifyNoInteractions(contaReceberService);
        verify(contaFinanceiraService, never()).buscar(any(), any());
        verify(contaFinanceiraService, never()).movimentarComOrigem(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void naoDeveAceitarChaveIdempotenciaVazia() {
        assertThrows(RegraNegocioException.class, () -> service().receberEmConta(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE, " "));
        verifyNoInteractions(contaReceberService, contaPagarService, contaFinanceiraService);
    }

    @Test
    void naoDeveMovimentarContaFinanceiraDeOutraFilial() {
        UUID tenantId = UUID.randomUUID(); UUID usuarioId = UUID.randomUUID(); UUID tituloId = UUID.randomUUID();
        UUID contaFinanceiraId = UUID.randomUUID(); BigDecimal valor = new BigDecimal("25.00"); String chave = "rec-2";
        ContaReceber titulo = new ContaReceber(tenantId, UUID.randomUUID(), UUID.randomUUID(), "R-2", "Receber",
                new BigDecimal("100.00"), LocalDate.now().plusDays(10), usuarioId);
        ContaFinanceira contaFinanceira = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", usuarioId);
        when(contaFinanceiraService.existeMovimentoPorOrigem(tenantId, "RECEBIMENTO_CONTA_RECEBER", tituloId, chave)).thenReturn(false);
        when(contaReceberService.buscar(tenantId, tituloId)).thenReturn(titulo);
        when(contaFinanceiraService.buscar(tenantId, contaFinanceiraId)).thenReturn(contaFinanceira);

        assertThrows(RegraNegocioException.class,
                () -> service().receberEmConta(tenantId, usuarioId, tituloId, contaFinanceiraId, valor, chave));
        verify(contaReceberService, never()).receber(tenantId, usuarioId, tituloId, valor);
    }

    @Test
    void deveDebitarSomenteValorParcialComOrigemIdempotente() {
        UUID tenantId = UUID.randomUUID(); UUID usuarioId = UUID.randomUUID(); UUID filialId = UUID.randomUUID();
        UUID tituloId = UUID.randomUUID(); UUID contaFinanceiraId = UUID.randomUUID();
        BigDecimal valorParcial = new BigDecimal("30.00"); String chave = "pagamento-001";
        ContaPagar titulo = new ContaPagar(tenantId, filialId, UUID.randomUUID(), "P-1", "Pagar",
                new BigDecimal("80.00"), LocalDate.now().plusDays(5), usuarioId);
        ContaFinanceira contaFinanceira = new ContaFinanceira(tenantId, filialId, "Banco", "BANCO", usuarioId);
        when(contaFinanceiraService.existeMovimentoPorOrigem(tenantId, "PAGAMENTO_CONTA_PAGAR", tituloId, chave)).thenReturn(false);
        when(contaPagarService.buscar(tenantId, tituloId)).thenReturn(titulo);
        when(contaFinanceiraService.buscar(tenantId, contaFinanceiraId)).thenReturn(contaFinanceira);
        when(contaPagarService.pagar(tenantId, usuarioId, tituloId, valorParcial)).thenReturn(titulo);

        service().pagarEmConta(tenantId, usuarioId, tituloId, contaFinanceiraId, valorParcial, chave);

        verify(contaPagarService).pagar(tenantId, usuarioId, tituloId, valorParcial);
        verify(contaFinanceiraService).movimentarComOrigem(
                tenantId, usuarioId, contaFinanceiraId, "SAIDA", valorParcial,
                "PAGAMENTO_CONTA_PAGAR:" + tituloId, "PAGAMENTO_CONTA_PAGAR", tituloId, chave);
    }

    private TesourariaBaixaApplicationService service() {
        return new TesourariaBaixaApplicationService(contaReceberService, contaPagarService, contaFinanceiraService);
    }
}
