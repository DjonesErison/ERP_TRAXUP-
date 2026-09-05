package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Mock AuditoriaApplicationService auditoria;

    private PedidoVendaApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaApplicationService(repository, itemRepository, filialRepository,
                pessoaRepository, estoqueMovimentacaoService, auditoria);
    }

    @Test
    void deveFaturarProdutoEGradeComBaixaDeEstoque() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getId()).thenReturn(pedidoId);
        when(pedido.getFilialId()).thenReturn(filialId);
        when(pedido.getStatus()).thenReturn("ABERTO");
        when(repository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        PedidoVendaItem produto = mock(PedidoVendaItem.class);
        when(produto.getProdutoId()).thenReturn(produtoId);
        when(produto.getGradeId()).thenReturn(null);
        when(produto.getQuantidade()).thenReturn(new BigDecimal("2.0000"));

        PedidoVendaItem grade = mock(PedidoVendaItem.class);
        when(grade.getGradeId()).thenReturn(gradeId);
        when(grade.getQuantidade()).thenReturn(new BigDecimal("1.0000"));

        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(tenantId, pedidoId))
                .thenReturn(List.of(produto, grade));

        service.faturar(tenantId, usuarioId, pedidoId);

        verify(estoqueMovimentacaoService).movimentar(eq(tenantId), eq(filialId), eq("PRODUTO"), eq(produtoId),
                eq("SAIDA"), eq(new BigDecimal("2.0000")), contains(pedidoId.toString()), eq(usuarioId));
        verify(estoqueMovimentacaoService).movimentar(eq(tenantId), eq(filialId), eq("GRADE"), eq(gradeId),
                eq("SAIDA"), eq(new BigDecimal("1.0000")), contains(pedidoId.toString()), eq(usuarioId));
        verify(pedido).faturar();
        verify(repository).save(pedido);
        verify(auditoria).registrar(eq(tenantId), eq(usuarioId), isNull(), eq(filialId),
                eq("FATURAR"), eq("PEDIDO_VENDA"), eq(pedidoId), eq("itens=2"));
    }

    @Test
    void naoDeveMovimentarEstoqueQuandoPedidoNaoEstaAberto() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getStatus()).thenReturn("FATURADO");
        when(repository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> service.faturar(tenantId, UUID.randomUUID(), pedidoId));
        verifyNoInteractions(estoqueMovimentacaoService);
        verify(itemRepository, never()).findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAsc(any(), any());
    }

    @Test
    void deveRespeitarIsolamentoPorTenantAoBuscarPedido() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(tenantId, pedidoId));
        verify(repository).findByIdAndTenantId(pedidoId, tenantId);
    }
}
