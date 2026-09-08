package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecebimentoCompraConcorrenciaApplicationServiceTest {
    @Mock PedidoCompraRepository pedidoRepository;
    @Mock PedidoCompraItemRepository pedidoItemRepository;
    @Mock RecebimentoCompraRepository recebimentoRepository;
    @Mock RecebimentoCompraItemRepository recebimentoItemRepository;
    @Mock EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveBloquearPedidoAntesDeValidarDuplicidadeDoRecebimento() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = pedidoAberto(tenantId);
        when(pedidoRepository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(recebimentoRepository.existsByTenantIdAndPedidoCompraId(tenantId, pedidoId)).thenReturn(true);

        assertThrows(RecursoConflitanteException.class, () -> service().registrar(
                tenantId, UUID.randomUUID(), pedidoId, null, null, null));

        verify(pedidoRepository).findByIdAndTenantIdForUpdate(pedidoId, tenantId);
        verify(recebimentoRepository).existsByTenantIdAndPedidoCompraId(tenantId, pedidoId);
        verify(recebimentoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveRejeitarRecebimentoQuandoPedidoNaoEstiverAberto() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoCompra pedido = new PedidoCompra(tenantId, UUID.randomUUID(), UUID.randomUUID(), "PC-1", null);
        when(pedidoRepository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));

        assertThrows(RegraNegocioException.class, () -> service().registrar(
                tenantId, UUID.randomUUID(), pedidoId, null, null, null));

        verify(recebimentoRepository, never()).existsByTenantIdAndPedidoCompraId(tenantId, pedidoId);
    }

    @Test
    void deveBloquearPedidoDuranteIntegracaoComEstoque() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID recebimentoId = UUID.randomUUID();
        PedidoCompra pedido = pedidoAberto(tenantId);
        RecebimentoCompra recebimento = new RecebimentoCompra(
                tenantId, pedidoId, pedido.getFilialId(), pedido.getFornecedorId(), null, null, UUID.randomUUID());
        when(recebimentoRepository.findByIdAndTenantId(recebimentoId, tenantId)).thenReturn(Optional.of(recebimento));
        when(pedidoRepository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)).thenReturn(Optional.of(pedido));
        when(recebimentoItemRepository.findAllByTenantIdAndRecebimentoIdOrderByCriadoEmAsc(tenantId, recebimentoId))
                .thenReturn(java.util.List.of());

        assertThrows(RegraNegocioException.class, () -> service().integrarEstoque(
                tenantId, UUID.randomUUID(), recebimentoId));

        verify(pedidoRepository).findByIdAndTenantIdForUpdate(pedidoId, tenantId);
        verify(estoqueMovimentacaoService, never()).movimentar(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private PedidoCompra pedidoAberto(UUID tenantId) {
        PedidoCompra pedido = new PedidoCompra(tenantId, UUID.randomUUID(), UUID.randomUUID(), "PC-OK", null);
        pedido.abrir();
        return pedido;
    }

    private RecebimentoCompraApplicationService service() {
        return new RecebimentoCompraApplicationService(
                pedidoRepository, pedidoItemRepository, recebimentoRepository, recebimentoItemRepository,
                estoqueMovimentacaoService, auditoria);
    }
}
