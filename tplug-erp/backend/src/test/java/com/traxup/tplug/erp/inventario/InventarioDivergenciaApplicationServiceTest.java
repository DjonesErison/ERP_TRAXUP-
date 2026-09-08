package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioDivergenciaApplicationServiceTest {
    @Mock InventarioApplicationService inventarioService;
    @Mock ProdutoRepository produtoRepository;
    @Mock GradeProdutoRepository gradeProdutoRepository;

    private InventarioDivergenciaApplicationService service() {
        return new InventarioDivergenciaApplicationService(inventarioService, produtoRepository, gradeProdutoRepository);
    }

    @Test
    void deveEnriquecerProdutoSemSairDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        InventarioContagem contagem = new InventarioContagem(tenantId, inventarioId, "PRODUTO", produtoId,
                BigDecimal.TEN, BigDecimal.ONE, null);
        Produto produto = mock(Produto.class);
        when(produto.getCodigo()).thenReturn("P001");
        when(produto.getDescricao()).thenReturn("Produto teste");
        when(inventarioService.listarDivergencias(tenantId, inventarioId, 50)).thenReturn(List.of(contagem));
        when(produtoRepository.findByIdAndTenantId(produtoId, tenantId)).thenReturn(Optional.of(produto));

        InventarioDivergenciaItem item = service().listar(tenantId, inventarioId, 50).getFirst();

        assertEquals("P001", item.codigoItem());
        assertEquals("Produto teste", item.descricaoItem());
        verify(produtoRepository).findByIdAndTenantId(produtoId, tenantId);
    }

    @Test
    void deveEnriquecerGradeSemSairDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        InventarioContagem contagem = new InventarioContagem(tenantId, inventarioId, "GRADE", gradeId,
                BigDecimal.TEN, BigDecimal.ONE, null);
        GradeProduto grade = mock(GradeProduto.class);
        when(grade.getCodigoGrade()).thenReturn("AZUL-M");
        when(grade.getDescricaoGrade()).thenReturn("Camiseta Azul M");
        when(inventarioService.listarDivergencias(tenantId, inventarioId, 25)).thenReturn(List.of(contagem));
        when(gradeProdutoRepository.findByIdAndTenantId(gradeId, tenantId)).thenReturn(Optional.of(grade));

        InventarioDivergenciaItem item = service().listar(tenantId, inventarioId, 25).getFirst();

        assertEquals("AZUL-M", item.codigoItem());
        assertEquals("Camiseta Azul M", item.descricaoItem());
        verify(gradeProdutoRepository).findByIdAndTenantId(gradeId, tenantId);
    }

    @Test
    void deveFalharQuandoItemNaoPertenceAoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        InventarioContagem contagem = new InventarioContagem(tenantId, inventarioId, "PRODUTO", produtoId,
                BigDecimal.TEN, BigDecimal.ONE, null);
        when(inventarioService.listarDivergencias(tenantId, inventarioId, 10)).thenReturn(List.of(contagem));
        when(produtoRepository.findByIdAndTenantId(produtoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service().listar(tenantId, inventarioId, 10));
    }
}
