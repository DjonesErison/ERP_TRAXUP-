package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoComboVigenciaApplicationServiceTest {
    private final ProdutoComboVigenciaRepository repository = mock(ProdutoComboVigenciaRepository.class);
    private final ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
    private final ProdutoComboComponenteRepository componenteRepository = mock(ProdutoComboComponenteRepository.class);
    private final ProdutoComboGrupoRepository grupoRepository = mock(ProdutoComboGrupoRepository.class);
    private final ProdutoComboVigenciaApplicationService service = new ProdutoComboVigenciaApplicationService(
            repository, produtoRepository, componenteRepository, grupoRepository);

    @Test
    void deveRejeitarVigenciaParaProdutoQueNaoEhCombo() {
        UUID tenant = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        Produto produto = mock(Produto.class);
        when(produtoRepository.findByIdAndTenantId(produtoId, tenant)).thenReturn(Optional.of(produto));
        when(componenteRepository.existsByTenantIdAndComboProdutoId(tenant, produtoId)).thenReturn(false);
        when(grupoRepository.existsByTenantIdAndComboProdutoId(tenant, produtoId)).thenReturn(false);

        assertThatThrownBy(() -> service.configurar(
                tenant, produtoId, Instant.parse("2026-09-08T10:00:00Z"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vigencia so pode ser configurada para produto combo");

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveAceitarComboComGrupoMesmoSemComponenteFixo() {
        UUID tenant = UUID.randomUUID();
        UUID comboId = UUID.randomUUID();
        Produto produto = mock(Produto.class);
        when(produtoRepository.findByIdAndTenantId(comboId, tenant)).thenReturn(Optional.of(produto));
        when(componenteRepository.existsByTenantIdAndComboProdutoId(tenant, comboId)).thenReturn(false);
        when(grupoRepository.existsByTenantIdAndComboProdutoId(tenant, comboId)).thenReturn(true);
        when(repository.findByTenantIdAndComboProdutoId(tenant, comboId)).thenReturn(Optional.empty());

        service.configurar(tenant, comboId, null, Instant.parse("2026-09-09T10:00:00Z"));

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(v ->
                tenant.equals(v.getTenantId()) && comboId.equals(v.getComboProdutoId())));
    }
}
