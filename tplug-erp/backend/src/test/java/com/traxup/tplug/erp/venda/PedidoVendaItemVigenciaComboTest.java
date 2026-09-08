package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.combo.ProdutoComboVigenciaApplicationService;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoVendaItemVigenciaComboTest {
    private final PedidoVendaRepository pedidoRepository = mock(PedidoVendaRepository.class);
    private final PedidoVendaItemRepository itemRepository = mock(PedidoVendaItemRepository.class);
    private final ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
    private final GradeProdutoRepository gradeRepository = mock(GradeProdutoRepository.class);
    private final ProdutoComboVigenciaApplicationService vigenciaService = mock(ProdutoComboVigenciaApplicationService.class);
    private final AuditoriaApplicationService auditoria = mock(AuditoriaApplicationService.class);
    private final PedidoVendaItemApplicationService service = new PedidoVendaItemApplicationService(
            pedidoRepository, itemRepository, produtoRepository, gradeRepository, vigenciaService, auditoria);

    @Test
    void deveRejeitarComboForaDaVigenciaParaNovaVenda() {
        UUID tenant = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        Produto produto = mock(Produto.class);

        when(pedido.getStatus()).thenReturn("RASCUNHO");
        when(pedidoRepository.findByIdAndTenantId(pedidoId, tenant)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findByIdAndTenantId(produtoId, tenant)).thenReturn(Optional.of(produto));
        when(produto.isAtivo()).thenReturn(true);
        when(vigenciaService.vigenteEm(eq(tenant), eq(produtoId), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> service.adicionar(
                tenant, usuario, pedidoId, produtoId, null, BigDecimal.ONE, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Produto combo esta fora da vigencia para novas vendas");

        verify(itemRepository, never()).save(any());
        verify(auditoria, never()).registrar(
                any(), any(), any(), any(), any(), any(), any(), any());
    }
}
