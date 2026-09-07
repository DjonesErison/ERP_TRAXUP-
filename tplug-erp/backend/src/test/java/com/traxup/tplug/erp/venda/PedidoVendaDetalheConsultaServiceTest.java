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

    @Test
    void deveBuscarPedidoUmaVezEListarItensNoMesmoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        PedidoVendaItem item = mock(PedidoVendaItem.class);
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId))
                .thenReturn(List.of(item));

        var detalhe = new PedidoVendaDetalheConsultaService(pedidoRepository, itemRepository)
                .consultar(tenantId, pedidoId);

        assertEquals(pedido, detalhe.pedido());
        assertEquals(List.of(item), detalhe.itens());
        verify(pedidoRepository).findByIdAndTenantId(pedidoId, tenantId);
        verify(itemRepository).findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
    }

    @Test
    void deveInterromperAntesDosItensQuandoPedidoNaoPertenceAoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> new PedidoVendaDetalheConsultaService(pedidoRepository, itemRepository)
                        .consultar(tenantId, pedidoId));

        verify(pedidoRepository).findByIdAndTenantId(pedidoId, tenantId);
        verifyNoInteractions(itemRepository);
    }
}
