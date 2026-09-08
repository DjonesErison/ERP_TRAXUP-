package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.estoque.EstoqueSaldo;
import com.traxup.tplug.erp.estoque.EstoqueSaldoRepository;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoComboDisponibilidadeServiceTest {
    private final ProdutoComboApplicationService comboService = mock(ProdutoComboApplicationService.class);
    private final EstoqueSaldoRepository estoqueRepository = mock(EstoqueSaldoRepository.class);
    private final FilialRepository filialRepository = mock(FilialRepository.class);
    private final ProdutoComboDisponibilidadeService service = new ProdutoComboDisponibilidadeService(
            comboService, estoqueRepository, filialRepository);

    @Test
    void deveCalcularPeloComponenteLimitante() {
        UUID tenant = UUID.randomUUID();
        UUID filial = UUID.randomUUID();
        UUID combo = UUID.randomUUID();
        UUID produtoA = UUID.randomUUID();
        UUID produtoB = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filial, tenant)).thenReturn(true);
        when(comboService.listar(tenant, combo)).thenReturn(List.of(
                new ProdutoComboComponente(tenant, combo, produtoA, new BigDecimal("2")),
                new ProdutoComboComponente(tenant, combo, produtoB, new BigDecimal("3"))));
        EstoqueSaldo saldoA = new EstoqueSaldo(tenant, filial, "PRODUTO", produtoA);
        saldoA.definirQuantidade(new BigDecimal("11"));
        EstoqueSaldo saldoB = new EstoqueSaldo(tenant, filial, "PRODUTO", produtoB);
        saldoB.definirQuantidade(new BigDecimal("13"));
        when(estoqueRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenant, filial, "PRODUTO", produtoA))
                .thenReturn(Optional.of(saldoA));
        when(estoqueRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenant, filial, "PRODUTO", produtoB))
                .thenReturn(Optional.of(saldoB));

        assertThat(service.calcular(tenant, filial, combo)).isEqualByComparingTo("4");
    }

    @Test
    void deveRetornarZeroQuandoComponenteNaoTemSaldo() {
        UUID tenant = UUID.randomUUID();
        UUID filial = UUID.randomUUID();
        UUID combo = UUID.randomUUID();
        UUID componente = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filial, tenant)).thenReturn(true);
        when(comboService.listar(tenant, combo)).thenReturn(List.of(
                new ProdutoComboComponente(tenant, combo, componente, BigDecimal.ONE)));
        when(estoqueRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenant, filial, "PRODUTO", componente))
                .thenReturn(Optional.empty());

        assertThat(service.calcular(tenant, filial, combo)).isEqualByComparingTo("0");
    }

    @Test
    void deveRejeitarFilialForaDoTenantAntesDeConsultarComboOuEstoque() {
        UUID tenant = UUID.randomUUID();
        UUID filial = UUID.randomUUID();
        UUID combo = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filial, tenant)).thenReturn(false);

        assertThatThrownBy(() -> service.calcular(tenant, filial, combo))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Filial nao encontrada para o tenant informado");

        verify(comboService, never()).listar(tenant, combo);
        verify(estoqueRepository, never()).findByTenantIdAndFilialIdAndTipoItemAndItemId(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
