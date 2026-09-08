package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoComboGrupoApplicationServiceTest {
    private final ProdutoComboGrupoRepository grupoRepository = mock(ProdutoComboGrupoRepository.class);
    private final ProdutoComboGrupoOpcaoRepository opcaoRepository = mock(ProdutoComboGrupoOpcaoRepository.class);
    private final ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
    private final ProdutoComboGrupoApplicationService service =
            new ProdutoComboGrupoApplicationService(grupoRepository, opcaoRepository, produtoRepository);

    @Test
    void deveCriarGrupoSomenteNoTenantDoCombo() {
        UUID tenant = UUID.randomUUID();
        UUID combo = UUID.randomUUID();
        when(produtoRepository.findByIdAndTenantId(combo, tenant)).thenReturn(Optional.of(mock(Produto.class)));
        when(grupoRepository.existsByTenantIdAndComboProdutoIdAndNomeIgnoreCase(tenant, combo, "Bebida"))
                .thenReturn(false);

        service.criarGrupo(tenant, combo, " Bebida ", 1, 1);

        verify(grupoRepository).save(any(ProdutoComboGrupo.class));
    }

    @Test
    void deveBloquearGrupoParaComboDeOutroTenant() {
        UUID tenant = UUID.randomUUID();
        UUID combo = UUID.randomUUID();
        when(produtoRepository.findByIdAndTenantId(combo, tenant)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarGrupo(tenant, combo, "Bebida", 1, 1))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(grupoRepository, never()).save(any());
    }

    @Test
    void deveBloquearComboComoOpcaoDeSiMesmo() {
        UUID tenant = UUID.randomUUID();
        UUID combo = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        ProdutoComboGrupo grupo = new ProdutoComboGrupo(tenant, combo, "Escolha", 1, 1);
        when(grupoRepository.findByIdAndTenantIdAndComboProdutoId(grupoId, tenant, combo)).thenReturn(Optional.of(grupo));
        when(produtoRepository.findByIdAndTenantId(combo, tenant)).thenReturn(Optional.of(mock(Produto.class)));

        assertThatThrownBy(() -> service.adicionarOpcao(tenant, combo, grupoId, combo, BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("si mesmo");
        verify(opcaoRepository, never()).save(any());
    }
}
