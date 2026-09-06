package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.financeiro.ContaReceberApplicationService;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.financeiro.pagamento.FormaPagamentoRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoVendaApplicationServiceTest {
    @Mock PedidoVendaRepository repository;
    @Mock PedidoVendaItemRepository itemRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    @Mock ContaReceberApplicationService contaReceberService;
    @Mock FormaPagamentoRepository formaPagamentoRepository;
    @Mock CondicaoPagamentoRepository condicaoPagamentoRepository;
    @Mock CondicaoPagamentoParcelaRepository parcelaRepository;
    @Mock AuditoriaApplicationService auditoria;

    private PedidoVendaApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaApplicationService(repository, itemRepository, filialRepository,
                pessoaRepository, estoqueMovimentacaoService, contaReceberService,
                formaPagamentoRepository, condicaoPagamentoRepository, parcelaRepository, auditoria);
    }

    @Test
    void deveFaturarProdutoEGradeComBaixaDeEstoque() {
        UUID tenantId = UUID.randomUUID(); UUID filialId = UUID.randomUUID(); UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID(); UUID gradeId = UUID.randomUUID(); UUID usuarioId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getId()).thenReturn(pedidoId); when(pedido.getFilialId()).thenReturn(filialId); when(pedido.getStatus()).thenReturn("ABERTO");
        when(repository.buscarParaFaturar(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        PedidoVendaItem produto = mock(PedidoVendaItem.class); when(produto.getProdutoId()).thenReturn(produtoId); when(produto.getGradeId()).thenReturn(null); when(produto.getQuantidade()).thenReturn(new BigDecimal("2.0000"));
        PedidoVendaItem grade = mock(PedidoVendaItem.class); when(grade.getGradeId()).thenReturn(gradeId); when(grade.getQuantidade()).thenReturn(new BigDecimal("1.0000"));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of(produto, grade));
        service.faturar(tenantId, usuarioId, pedidoId);
        verify(repository).buscarParaFaturar(pedidoId, tenantId);
        verify(estoqueMovimentacaoService).movimentar(eq(tenantId), eq(filialId), eq("PRODUTO"), eq(produtoId), eq("SAIDA"), eq(new BigDecimal("2.0000")), contains(pedidoId.toString()), eq(usuarioId));
        verify(estoqueMovimentacaoService).movimentar(eq(tenantId), eq(filialId), eq("GRADE"), eq(gradeId), eq("SAIDA"), eq(new BigDecimal("1.0000")), contains(pedidoId.toString()), eq(usuarioId));
        verifyNoInteractions(contaReceberService); verify(pedido).faturar(); verify(repository).save(pedido);
    }

    @Test
    void deveGerarContaReceberAoFaturarPedidoComClienteSemCondicaoConfigurada() {
        UUID tenantId = UUID.randomUUID(); UUID filialId = UUID.randomUUID(); UUID pedidoId = UUID.randomUUID(); UUID clienteId = UUID.randomUUID(); UUID usuarioId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getId()).thenReturn(pedidoId); when(pedido.getFilialId()).thenReturn(filialId); when(pedido.getClienteId()).thenReturn(clienteId); when(pedido.getNumero()).thenReturn("PV-100"); when(pedido.getStatus()).thenReturn("ABERTO");
        when(repository.buscarParaFaturar(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        PedidoVendaItem item = mock(PedidoVendaItem.class); when(item.getProdutoId()).thenReturn(UUID.randomUUID()); when(item.getGradeId()).thenReturn(null); when(item.getQuantidade()).thenReturn(BigDecimal.ONE); when(item.getTotalItem()).thenReturn(new BigDecimal("125.50"));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of(item));
        service.faturar(tenantId, usuarioId, pedidoId);
        verify(contaReceberService).criarComOrigem(eq(tenantId), eq(usuarioId), eq(filialId), eq(clienteId),
                eq("PV-PV-100-1"), contains("parcela 1"), eq(new BigDecimal("125.5000")), eq(LocalDate.now()),
                eq("PEDIDO_VENDA"), eq(pedidoId), eq("PARCELA:1"));
    }

    @Test
    void deveGerarUmaContaReceberPorParcelaDaCondicao() {
        UUID tenantId = UUID.randomUUID(); UUID filialId = UUID.randomUUID(); UUID pedidoId = UUID.randomUUID(); UUID clienteId = UUID.randomUUID(); UUID usuarioId = UUID.randomUUID(); UUID condicaoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getId()).thenReturn(pedidoId); when(pedido.getFilialId()).thenReturn(filialId); when(pedido.getClienteId()).thenReturn(clienteId); when(pedido.getNumero()).thenReturn("200"); when(pedido.getStatus()).thenReturn("ABERTO"); when(pedido.getCondicaoPagamentoId()).thenReturn(condicaoId);
        when(repository.buscarParaFaturar(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        PedidoVendaItem item = mock(PedidoVendaItem.class); when(item.getProdutoId()).thenReturn(UUID.randomUUID()); when(item.getGradeId()).thenReturn(null); when(item.getQuantidade()).thenReturn(BigDecimal.ONE); when(item.getTotalItem()).thenReturn(new BigDecimal("100.0000"));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of(item));
        when(condicaoPagamentoRepository.findByIdAndTenantId(condicaoId, tenantId)).thenReturn(Optional.of(new CondicaoPagamento(tenantId, "2X", "Duas parcelas")));
        CondicaoPagamentoParcela primeira = new CondicaoPagamentoParcela(tenantId, condicaoId, 1, 30, new BigDecimal("50.0000"));
        CondicaoPagamentoParcela segunda = new CondicaoPagamentoParcela(tenantId, condicaoId, 2, 60, new BigDecimal("50.0000"));
        when(parcelaRepository.findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(tenantId, condicaoId)).thenReturn(List.of(primeira, segunda));
        service.faturar(tenantId, usuarioId, pedidoId);
        verify(contaReceberService).criarComOrigem(eq(tenantId), eq(usuarioId), eq(filialId), eq(clienteId),
                eq("PV-200-1"), contains("parcela 1"), eq(new BigDecimal("50.0000")), eq(LocalDate.now().plusDays(30)),
                eq("PEDIDO_VENDA"), eq(pedidoId), eq("PARCELA:1"));
        verify(contaReceberService).criarComOrigem(eq(tenantId), eq(usuarioId), eq(filialId), eq(clienteId),
                eq("PV-200-2"), contains("parcela 2"), eq(new BigDecimal("50.0000")), eq(LocalDate.now().plusDays(60)),
                eq("PEDIDO_VENDA"), eq(pedidoId), eq("PARCELA:2"));
    }

    @Test
    void naoDeveMovimentarEstoqueQuandoPedidoNaoEstaAberto() {
        UUID tenantId = UUID.randomUUID(); UUID pedidoId = UUID.randomUUID(); PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getStatus()).thenReturn("FATURADO"); when(repository.buscarParaFaturar(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class, () -> service.faturar(tenantId, UUID.randomUUID(), pedidoId));
        verify(repository).buscarParaFaturar(pedidoId, tenantId);
        verifyNoInteractions(estoqueMovimentacaoService); verifyNoInteractions(contaReceberService); verify(itemRepository, never()).findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(any(), any());
    }

    @Test
    void deveRespeitarIsolamentoPorTenantAoFaturarPedido() {
        UUID tenantId = UUID.randomUUID(); UUID pedidoId = UUID.randomUUID();
        when(repository.buscarParaFaturar(pedidoId, tenantId)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.faturar(tenantId, UUID.randomUUID(), pedidoId));
        verify(repository).buscarParaFaturar(pedidoId, tenantId);
        verifyNoInteractions(estoqueMovimentacaoService, contaReceberService);
    }

    @Test
    void deveRespeitarIsolamentoPorTenantAoBuscarPedido() {
        UUID tenantId = UUID.randomUUID(); UUID pedidoId = UUID.randomUUID(); when(repository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(tenantId, pedidoId)); verify(repository).findByIdAndTenantId(pedidoId, tenantId);
    }
}
