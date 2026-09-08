package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaDetalheConsultaServiceTest {
    @Mock PedidoVendaRepository pedidoRepository;
    @Mock PedidoVendaItemRepository itemRepository;
    @Mock PedidoVendaItemComboOpcaoRepository comboOpcaoRepository;

    @Test
    void deveBuscarPedidoItensEOpcoesNoMesmoTenantEmLote() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        PedidoVendaItem item = mock(PedidoVendaItem.class);
        PedidoVendaItemComboOpcao opcao = mock(PedidoVendaItemComboOpcao.class);
        when(item.getId()).thenReturn(itemId);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId))
                .thenReturn(List.of(item));
        when(comboOpcaoRepository
                .findAllByTenantIdAndPedidoVendaItemIdInOrderByPedidoVendaItemIdAscGrupoIdAscOpcaoIdAsc(
                        tenantId, List.of(itemId)))
                .thenReturn(List.of(opcao));

        var detalhe = new PedidoVendaDetalheConsultaService(pedidoRepository, itemRepository, comboOpcaoRepository)
                .consultar(tenantId, pedidoId);

        assertEquals(pedido, detalhe.pedido());
        assertEquals(List.of(item), detalhe.itens());
        assertEquals(List.of(opcao), detalhe.comboOpcoes());
        verify(pedidoRepository).findByIdAndTenantId(pedidoId, tenantId);
        verify(itemRepository).findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
        verify(comboOpcaoRepository)
                .findAllByTenantIdAndPedidoVendaItemIdInOrderByPedidoVendaItemIdAscGrupoIdAscOpcaoIdAsc(
                        tenantId, List.of(itemId));
    }

    @Test
    void naoDeveConsultarOpcoesQuandoPedidoNaoTemItens() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId))
                .thenReturn(List.of());

        var detalhe = new PedidoVendaDetalheConsultaService(pedidoRepository, itemRepository, comboOpcaoRepository)
                .consultar(tenantId, pedidoId);

        assertEquals(List.of(), detalhe.itens());
        assertEquals(List.of(), detalhe.comboOpcoes());
        verifyNoInteractions(comboOpcaoRepository);
    }

    @Test
    void deveInterromperAntesDosItensQuandoPedidoNaoPertenceAoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> new PedidoVendaDetalheConsultaService(pedidoRepository, itemRepository, comboOpcaoRepository)
                        .consultar(tenantId, pedidoId));

        verify(pedidoRepository).findByIdAndTenantId(pedidoId, tenantId);
        verifyNoInteractions(itemRepository, comboOpcaoRepository);
    }
}
