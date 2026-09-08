package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoCompraFluxoApplicationServiceTest {
    @Mock PedidoCompraRepository repository;
    @Mock PedidoCompraItemRepository itemRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveAbrirPedidoComBloqueioQuandoPossuiItens() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = pedido(tenantId);
        PedidoCompraItem item = org.mockito.Mockito.mock(PedidoCompraItem.class);
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of(item));

        PedidoCompra resultado = service().abrir(tenantId, UUID.randomUUID(), pedidoId);

        assertEquals("ABERTO", resultado.getStatus());
        verify(repository).findByIdAndTenantIdForUpdate(pedidoId, tenantId);
        verify(repository).save(pedido);
    }

    @Test
    void naoDeveAbrirPedidoSemItens() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = pedido(tenantId);
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId)).thenReturn(List.of());

        assertThrows(RegraNegocioException.class,
                () -> service().abrir(tenantId, UUID.randomUUID(), pedidoId));

        verify(repository, never()).save(pedido);
    }

    @Test
    void naoDeveCancelarPedidoJaCancelado() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = pedido(tenantId);
        pedido.cancelar();
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(RecursoConflitanteException.class,
                () -> service().cancelar(tenantId, UUID.randomUUID(), pedidoId));

        verify(repository, never()).save(pedido);
    }

    @Test
    void naoDeveMarcarRecebidoPedidoQueNaoEstaAberto() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = pedido(tenantId);
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(RegraNegocioException.class,
                () -> service().marcarRecebido(tenantId, UUID.randomUUID(), pedidoId));

        verify(repository, never()).save(pedido);
    }

    private PedidoCompra pedido(UUID tenantId) {
        return new PedidoCompra(tenantId, UUID.randomUUID(), UUID.randomUUID(), "PC-1", null);
    }

    private PedidoCompraApplicationService service() {
        return new PedidoCompraApplicationService(repository, itemRepository, filialRepository, pessoaRepository, auditoria);
    }
}
