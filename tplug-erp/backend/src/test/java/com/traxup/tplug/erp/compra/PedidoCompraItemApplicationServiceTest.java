package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoCompraItemApplicationServiceTest {

    private final PedidoCompraRepository pedidoRepository = mock(PedidoCompraRepository.class);
    private final PedidoCompraItemRepository itemRepository = mock(PedidoCompraItemRepository.class);
    private final ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
    private final GradeProdutoRepository gradeRepository = mock(GradeProdutoRepository.class);
    private final AuditoriaApplicationService auditoria = mock(AuditoriaApplicationService.class);
    private final PedidoCompraItemApplicationService service = new PedidoCompraItemApplicationService(
            pedidoRepository, itemRepository, produtoRepository, gradeRepository, auditoria);

    @Test
    void deveAdicionarProdutoAtivoEmPedidoRascunhoEAuditar() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();

        PedidoCompra pedido = mock(PedidoCompra.class);
        Produto produto = mock(Produto.class);
        when(pedido.getStatus()).thenReturn("RASCUNHO");
        when(pedido.getFilialId()).thenReturn(filialId);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findByIdAndTenantId(produtoId, tenantId)).thenReturn(Optional.of(produto));
        when(produto.isAtivo()).thenReturn(true);
        when(itemRepository.save(any(PedidoCompraItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoCompraItem item = service.adicionar(
                tenantId, usuarioId, pedidoId, produtoId, null,
                new BigDecimal("2.0000"), new BigDecimal("10.5000"));

        assertEquals(new BigDecimal("21.00000000"), item.getTotalItem());
        verify(auditoria).registrar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveBloquearAlteracaoForaDoRascunho() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = mock(PedidoCompra.class);
        when(pedido.getStatus()).thenReturn("RECEBIDO");
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> service.adicionar(
                tenantId, UUID.randomUUID(), pedidoId, UUID.randomUUID(), null,
                BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    void naoDeveAcessarPedidoDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.listar(tenantId, pedidoId));
    }
}
