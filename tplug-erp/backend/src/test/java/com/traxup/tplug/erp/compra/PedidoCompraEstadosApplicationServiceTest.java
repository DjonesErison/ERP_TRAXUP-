package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
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
class PedidoCompraEstadosApplicationServiceTest {
    @Mock PedidoCompraRepository repository;
    @Mock PedidoCompraItemRepository itemRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBloquearPedidoAntesDeAbrir() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = novoPedido(tenantId);
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(itemRepository.findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId))
                .thenReturn(List.of(org.mockito.Mockito.mock(PedidoCompraItem.class)));

        PedidoCompra resultado = novoService().abrir(tenantId, UUID.randomUUID(), pedidoId);

        assertEquals("ABERTO", resultado.getStatus());
        verify(repository).findByIdAndTenantIdForUpdate(pedidoId, tenantId);
        verify(repository).save(pedido);
    }

    @Test
    void naoDeveAbrirPedidoQueJaFoiAberto() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = novoPedido(tenantId);
        pedido.abrir();
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(RegraNegocioException.class,
                () -> novoService().abrir(tenantId, UUID.randomUUID(), pedidoId));

        verify(itemRepository, never()).findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId);
        verify(repository, never()).save(pedido);
    }

    @Test
    void deveBloquearPedidoAntesDeCancelar() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = novoPedido(tenantId);
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        PedidoCompra resultado = novoService().cancelar(tenantId, UUID.randomUUID(), pedidoId);

        assertEquals("CANCELADO", resultado.getStatus());
        verify(repository).findByIdAndTenantIdForUpdate(pedidoId, tenantId);
        verify(repository).save(pedido);
    }

    @Test
    void naoDeveCancelarPedidoRecebido() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = novoPedido(tenantId);
        pedido.abrir();
        pedido.marcarRecebido();
        when(repository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(RegraNegocioException.class,
                () -> novoService().cancelar(tenantId, UUID.randomUUID(), pedidoId));

        verify(repository, never()).save(pedido);
    }

    private PedidoCompra novoPedido(UUID tenantId) {
        return new PedidoCompra(tenantId, UUID.randomUUID(), UUID.randomUUID(), "PC-ESTADOS", null);
    }

    private PedidoCompraApplicationService novoService() {
        return new PedidoCompraApplicationService(repository, itemRepository, filialRepository, pessoaRepository, auditoria);
    }
}
