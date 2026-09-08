package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioLeituraApplicationServiceTest {
    @Mock ProdutoRepository produtoRepository;
    @Mock GradeProdutoRepository gradeProdutoRepository;

    private InventarioLeituraApplicationService service() {
        return new InventarioLeituraApplicationService(produtoRepository, gradeProdutoRepository);
    }

    @Test
    void devePriorizarGradeQuandoCodigoDeBarrasExisteNaGrade() {
        UUID tenantId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        GradeProduto grade = mock(GradeProduto.class);
        when(grade.getId()).thenReturn(gradeId);
        when(grade.getCodigoGrade()).thenReturn("AZUL-M");
        when(grade.getDescricaoGrade()).thenReturn("Camiseta Azul M");
        when(grade.getCodigoBarra()).thenReturn("789123");
        when(gradeProdutoRepository.findFirstByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, "789123"))
                .thenReturn(Optional.of(grade));

        InventarioItemLeitura item = service().localizarPorCodigoBarra(tenantId, " 789123 ");

        assertEquals("GRADE", item.tipoItem());
        assertEquals(gradeId, item.itemId());
        verify(produtoRepository, never()).findFirstByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, "789123");
    }

    @Test
    void deveLocalizarProdutoQuandoNaoHaGrade() {
        UUID tenantId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        Produto produto = mock(Produto.class);
        when(produto.getId()).thenReturn(produtoId);
        when(produto.getCodigo()).thenReturn("P001");
        when(produto.getDescricao()).thenReturn("Produto teste");
        when(produto.getCodigoBarra()).thenReturn("789456");
        when(gradeProdutoRepository.findFirstByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, "789456"))
                .thenReturn(Optional.empty());
        when(produtoRepository.findFirstByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, "789456"))
                .thenReturn(Optional.of(produto));

        InventarioItemLeitura item = service().localizarPorCodigoBarra(tenantId, "789456");

        assertEquals("PRODUTO", item.tipoItem());
        assertEquals(produtoId, item.itemId());
    }

    @Test
    void deveRejeitarCodigoVazioAntesDosRepositorios() {
        assertThrows(RegraNegocioException.class,
                () -> service().localizarPorCodigoBarra(UUID.randomUUID(), "  "));
    }

    @Test
    void deveInformarQuandoCodigoNaoExisteNoTenant() {
        UUID tenantId = UUID.randomUUID();
        when(gradeProdutoRepository.findFirstByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, "999"))
                .thenReturn(Optional.empty());
        when(produtoRepository.findFirstByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, "999"))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service().localizarPorCodigoBarra(tenantId, "999"));
    }
}
