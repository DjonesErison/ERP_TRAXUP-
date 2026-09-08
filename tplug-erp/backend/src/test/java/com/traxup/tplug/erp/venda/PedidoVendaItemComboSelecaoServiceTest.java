package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupo;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoOpcao;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoOpcaoRepository;
import com.traxup.tplug.erp.produto.combo.ProdutoComboGrupoRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaItemComboSelecaoServiceTest {
    @Mock PedidoVendaRepository pedidoRepository;
    @Mock PedidoVendaItemRepository itemRepository;
    @Mock ProdutoComboGrupoRepository grupoRepository;
    @Mock ProdutoComboGrupoOpcaoRepository opcaoRepository;
    @Mock PedidoVendaItemComboOpcaoRepository selecaoRepository;
    @Mock AuditoriaApplicationService auditoria;

    private PedidoVendaItemComboSelecaoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaItemComboSelecaoService(
                pedidoRepository, itemRepository, grupoRepository, opcaoRepository, selecaoRepository, auditoria);
    }

    @Test
    void devePersistirOpcaoValidaDoGrupoComLockNoPedido() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID produtoComboId = UUID.randomUUID();
        PedidoVenda pedido = new PedidoVenda(tenantId, filialId, null, "PV-1", null, usuarioId);
        PedidoVendaItem item = new PedidoVendaItem(tenantId, pedido.getId(), produtoComboId, null,
                BigDecimal.ONE, BigDecimal.TEN);
        ProdutoComboGrupo grupo = new ProdutoComboGrupo(tenantId, produtoComboId, "Bebida", 1, 1);
        ProdutoComboGrupoOpcao opcao = new ProdutoComboGrupoOpcao(tenantId, grupo.getId(), UUID.randomUUID(),
                BigDecimal.ONE, BigDecimal.ZERO);

        when(pedidoRepository.buscarParaAtualizar(pedido.getId(), tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findByIdAndTenantIdAndPedidoVendaId(item.getId(), tenantId, pedido.getId()))
                .thenReturn(Optional.of(item));
        when(grupoRepository.findAllByTenantIdAndComboProdutoIdOrderByNomeAsc(tenantId, produtoComboId))
                .thenReturn(List.of(grupo));
        when(opcaoRepository.findByIdAndTenantId(opcao.getId(), tenantId)).thenReturn(Optional.of(opcao));
        when(selecaoRepository.findAllByTenantIdAndPedidoVendaItemIdOrderByGrupoIdAscOpcaoIdAsc(tenantId, item.getId()))
                .thenReturn(List.of());

        service.configurar(tenantId, usuarioId, pedido.getId(), item.getId(), List.of(opcao.getId()));

        verify(pedidoRepository).buscarParaAtualizar(pedido.getId(), tenantId);
        verify(selecaoRepository).deleteByTenantIdAndPedidoVendaItemId(tenantId, item.getId());
        verify(selecaoRepository).flush();
        verify(selecaoRepository).save(any(PedidoVendaItemComboOpcao.class));
    }

    @Test
    void deveExigirMinimoDeEscolhasDoGrupo() {
        UUID tenantId = UUID.randomUUID();
        UUID produtoComboId = UUID.randomUUID();
        PedidoVenda pedido = new PedidoVenda(tenantId, UUID.randomUUID(), null, "PV-2", null, UUID.randomUUID());
        PedidoVendaItem item = new PedidoVendaItem(tenantId, pedido.getId(), produtoComboId, null,
                BigDecimal.ONE, BigDecimal.TEN);
        ProdutoComboGrupo grupo = new ProdutoComboGrupo(tenantId, produtoComboId, "Acompanhamento", 1, 2);

        when(pedidoRepository.buscarParaAtualizar(pedido.getId(), tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findByIdAndTenantIdAndPedidoVendaId(item.getId(), tenantId, pedido.getId()))
                .thenReturn(Optional.of(item));
        when(grupoRepository.findAllByTenantIdAndComboProdutoIdOrderByNomeAsc(tenantId, produtoComboId))
                .thenReturn(List.of(grupo));

        assertThrows(IllegalArgumentException.class,
                () -> service.configurar(tenantId, UUID.randomUUID(), pedido.getId(), item.getId(), List.of()));
        verify(selecaoRepository, never()).deleteByTenantIdAndPedidoVendaItemId(any(), any());
    }

    @Test
    void deveRejeitarOpcaoForaDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID produtoComboId = UUID.randomUUID();
        UUID opcaoId = UUID.randomUUID();
        PedidoVenda pedido = new PedidoVenda(tenantId, UUID.randomUUID(), null, "PV-3", null, UUID.randomUUID());
        PedidoVendaItem item = new PedidoVendaItem(tenantId, pedido.getId(), produtoComboId, null,
                BigDecimal.ONE, BigDecimal.TEN);
        ProdutoComboGrupo grupo = new ProdutoComboGrupo(tenantId, produtoComboId, "Bebida", 1, 1);

        when(pedidoRepository.buscarParaAtualizar(pedido.getId(), tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findByIdAndTenantIdAndPedidoVendaId(item.getId(), tenantId, pedido.getId()))
                .thenReturn(Optional.of(item));
        when(grupoRepository.findAllByTenantIdAndComboProdutoIdOrderByNomeAsc(tenantId, produtoComboId))
                .thenReturn(List.of(grupo));
        when(opcaoRepository.findByIdAndTenantId(opcaoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.configurar(tenantId, UUID.randomUUID(), pedido.getId(), item.getId(), List.of(opcaoId)));
        verify(selecaoRepository, never()).deleteByTenantIdAndPedidoVendaItemId(any(), any());
    }
}
