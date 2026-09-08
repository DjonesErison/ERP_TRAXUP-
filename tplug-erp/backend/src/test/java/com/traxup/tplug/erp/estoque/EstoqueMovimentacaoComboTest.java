package com.traxup.tplug.erp.estoque;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.produto.combo.ProdutoComboComponente;
import com.traxup.tplug.erp.produto.combo.ProdutoComboComponenteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstoqueMovimentacaoComboTest {

    private final EstoqueSaldoRepository saldoRepository = mock(EstoqueSaldoRepository.class);
    private final EstoqueMovimentacaoRepository movimentacaoRepository = mock(EstoqueMovimentacaoRepository.class);
    private final FilialRepository filialRepository = mock(FilialRepository.class);
    private final ProdutoComboComponenteRepository comboRepository = mock(ProdutoComboComponenteRepository.class);
    private final EstoqueMovimentacaoApplicationService service = new EstoqueMovimentacaoApplicationService(
            saldoRepository, movimentacaoRepository, filialRepository, comboRepository);

    @Test
    void deveBaixarComponentesSemBaixarSaldoDoProdutoCombo() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID comboId = UUID.randomUUID();
        UUID componenteAId = UUID.randomUUID();
        UUID componenteBId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(comboRepository.findAllByTenantIdAndComboProdutoIdOrderByComponenteProdutoIdAsc(tenantId, comboId))
                .thenReturn(List.of(
                        new ProdutoComboComponente(tenantId, comboId, componenteAId, new BigDecimal("2")),
                        new ProdutoComboComponente(tenantId, comboId, componenteBId, new BigDecimal("3"))));

        EstoqueSaldo saldoA = new EstoqueSaldo(tenantId, filialId, "PRODUTO", componenteAId);
        saldoA.definirQuantidade(new BigDecimal("10"));
        EstoqueSaldo saldoB = new EstoqueSaldo(tenantId, filialId, "PRODUTO", componenteBId);
        saldoB.definirQuantidade(new BigDecimal("20"));
        when(saldoRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenantId, filialId, "PRODUTO", componenteAId))
                .thenReturn(Optional.of(saldoA));
        when(saldoRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenantId, filialId, "PRODUTO", componenteBId))
                .thenReturn(Optional.of(saldoB));

        service.movimentarSaidaVenda(tenantId, filialId, "PRODUTO", comboId, new BigDecimal("2"),
                "FATURAMENTO_PEDIDO_VENDA:teste", usuarioId);

        assertThat(saldoA.getQuantidade()).isEqualByComparingTo("6");
        assertThat(saldoB.getQuantidade()).isEqualByComparingTo("14");
        verify(saldoRepository, never()).findByTenantIdAndFilialIdAndTipoItemAndItemId(
                tenantId, filialId, "PRODUTO", comboId);
        verify(movimentacaoRepository, org.mockito.Mockito.times(2)).save(any(EstoqueMovimentacao.class));
    }

    @Test
    void deveManterBaixaDiretaParaProdutoSemCombo() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(true);
        when(comboRepository.findAllByTenantIdAndComboProdutoIdOrderByComponenteProdutoIdAsc(tenantId, produtoId))
                .thenReturn(List.of());
        EstoqueSaldo saldo = new EstoqueSaldo(tenantId, filialId, "PRODUTO", produtoId);
        saldo.definirQuantidade(new BigDecimal("5"));
        when(saldoRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenantId, filialId, "PRODUTO", produtoId))
                .thenReturn(Optional.of(saldo));

        service.movimentarSaidaVenda(tenantId, filialId, "PRODUTO", produtoId, new BigDecimal("2"),
                "FATURAMENTO_PEDIDO_VENDA:teste", UUID.randomUUID());

        assertThat(saldo.getQuantidade()).isEqualByComparingTo("3");
    }
}
