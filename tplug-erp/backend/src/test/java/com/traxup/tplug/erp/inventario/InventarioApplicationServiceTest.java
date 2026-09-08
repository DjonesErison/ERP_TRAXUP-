package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueSaldo;
import com.traxup.tplug.erp.estoque.EstoqueSaldoRepository;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioApplicationServiceTest {
    @Mock InventarioSessaoRepository sessaoRepository;
    @Mock InventarioContagemRepository contagemRepository;
    @Mock EstoqueSaldoRepository estoqueSaldoRepository;
    @Mock EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    @Mock FilialRepository filialRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock GradeProdutoRepository gradeProdutoRepository;
    @Mock AuditoriaApplicationService auditoria;

    private InventarioApplicationService service() {
        return new InventarioApplicationService(sessaoRepository, contagemRepository, estoqueSaldoRepository,
                estoqueMovimentacaoService, filialRepository, produtoRepository, gradeProdutoRepository, auditoria);
    }

    @Test
    void deveRegistrarContagemComSnapshotEDivergencia() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, filialId, "Contagem", usuarioId);
        EstoqueSaldo saldo = mock(EstoqueSaldo.class);

        when(sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)).thenReturn(Optional.of(sessao));
        when(produtoRepository.findByIdAndTenantId(produtoId, tenantId)).thenReturn(Optional.of(mock(Produto.class)));
        when(estoqueSaldoRepository.findByTenantIdAndFilialIdAndTipoItemAndItemId(tenantId, filialId, "PRODUTO", produtoId))
                .thenReturn(Optional.of(saldo));
        when(saldo.getQuantidade()).thenReturn(new BigDecimal("10.0000"));
        when(contagemRepository.findByTenantIdAndInventarioIdAndTipoItemAndItemId(tenantId, inventarioId, "PRODUTO", produtoId))
                .thenReturn(Optional.empty());
        when(contagemRepository.save(any(InventarioContagem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventarioContagem contagem = service().registrarContagem(
                tenantId, usuarioId, inventarioId, "produto", produtoId, new BigDecimal("7.0000"));

        assertEquals(new BigDecimal("10.0000"), contagem.getQuantidadeSistema());
        assertEquals(new BigDecimal("7.0000"), contagem.getQuantidadeContada());
        assertEquals(new BigDecimal("-3.0000"), contagem.getDivergencia());
        verify(contagemRepository).save(any(InventarioContagem.class));
    }

    @Test
    void deveListarSomenteDivergenciasDaSessao() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, UUID.randomUUID(), null, null);
        InventarioContagem divergente = new InventarioContagem(tenantId, inventarioId, "PRODUTO", UUID.randomUUID(),
                new BigDecimal("10.0000"), new BigDecimal("8.0000"), null);

        when(sessaoRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(sessao));
        when(contagemRepository.findAllByTenantIdAndInventarioIdAndDivergenciaNotOrderByTipoItemAscItemIdAsc(
                tenantId, inventarioId, BigDecimal.ZERO, PageRequest.of(0, 50)))
                .thenReturn(List.of(divergente));

        List<InventarioContagem> resultado = service().listarDivergencias(tenantId, inventarioId, 50);

        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("-2.0000"), resultado.getFirst().getDivergencia());
    }

    @Test
    void deveBloquearContagemQuandoInventarioNaoEstaAberto() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, UUID.randomUUID(), null, null);
        sessao.concluir(null);
        when(sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)).thenReturn(Optional.of(sessao));

        assertThrows(RegraNegocioException.class, () -> service().registrarContagem(
                tenantId, null, inventarioId, "PRODUTO", UUID.randomUUID(), BigDecimal.ONE));

        verifyNoInteractions(produtoRepository, gradeProdutoRepository, estoqueSaldoRepository, contagemRepository);
    }

    @Test
    void deveExigirContagemAntesDeConcluir() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, UUID.randomUUID(), null, null);
        when(sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)).thenReturn(Optional.of(sessao));
        when(contagemRepository.existsByTenantIdAndInventarioId(tenantId, inventarioId)).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> service().concluir(tenantId, null, inventarioId));
        verify(sessaoRepository, never()).save(any(InventarioSessao.class));
    }

    @Test
    void deveAplicarContagemAoEstoqueUmaUnicaVezComMovimentacao() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, filialId, null, usuarioId);
        sessao.concluir(usuarioId);
        InventarioContagem contagem = new InventarioContagem(tenantId, inventarioId, "PRODUTO", produtoId,
                new BigDecimal("10.0000"), new BigDecimal("7.0000"), usuarioId);
        EstoqueSaldo saldo = new EstoqueSaldo(tenantId, filialId, "PRODUTO", produtoId);
        saldo.definirQuantidade(new BigDecimal("10.0000"));

        when(sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)).thenReturn(Optional.of(sessao));
        when(contagemRepository.findAllByTenantIdAndInventarioIdOrderByTipoItemAscItemIdAsc(tenantId, inventarioId))
                .thenReturn(List.of(contagem));
        when(estoqueSaldoRepository.buscarParaAtualizar(tenantId, filialId, "PRODUTO", produtoId))
                .thenReturn(Optional.of(saldo));
        when(sessaoRepository.save(any(InventarioSessao.class))).thenAnswer(inv -> inv.getArgument(0));

        InventarioSessao ajustada = service().ajustarEstoque(tenantId, usuarioId, inventarioId);

        assertNotNull(ajustada.getAjustadoEm());
        assertEquals(usuarioId, ajustada.getAjustadoPorId());
        verify(estoqueMovimentacaoService).movimentar(tenantId, filialId, "PRODUTO", produtoId,
                "AJUSTE", new BigDecimal("7.0000"), "INVENTARIO:" + inventarioId, usuarioId);

        assertThrows(RegraNegocioException.class,
                () -> service().ajustarEstoque(tenantId, usuarioId, inventarioId));
    }

    @Test
    void naoDeveGerarMovimentacaoQuandoSaldoJaConfereComContagem() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, filialId, null, usuarioId);
        sessao.concluir(usuarioId);
        InventarioContagem contagem = new InventarioContagem(tenantId, inventarioId, "PRODUTO", produtoId,
                new BigDecimal("7.0000"), new BigDecimal("7.0000"), usuarioId);
        EstoqueSaldo saldo = new EstoqueSaldo(tenantId, filialId, "PRODUTO", produtoId);
        saldo.definirQuantidade(new BigDecimal("7.0000"));

        when(sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)).thenReturn(Optional.of(sessao));
        when(contagemRepository.findAllByTenantIdAndInventarioIdOrderByTipoItemAscItemIdAsc(tenantId, inventarioId))
                .thenReturn(List.of(contagem));
        when(estoqueSaldoRepository.buscarParaAtualizar(tenantId, filialId, "PRODUTO", produtoId))
                .thenReturn(Optional.of(saldo));
        when(sessaoRepository.save(any(InventarioSessao.class))).thenAnswer(inv -> inv.getArgument(0));

        service().ajustarEstoque(tenantId, usuarioId, inventarioId);

        verifyNoInteractions(estoqueMovimentacaoService);
    }

    @Test
    void deveRejeitarAjusteDeInventarioAberto() {
        UUID tenantId = UUID.randomUUID();
        UUID inventarioId = UUID.randomUUID();
        InventarioSessao sessao = new InventarioSessao(tenantId, UUID.randomUUID(), null, null);
        when(sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)).thenReturn(Optional.of(sessao));

        assertThrows(RegraNegocioException.class,
                () -> service().ajustarEstoque(tenantId, null, inventarioId));
        verifyNoInteractions(contagemRepository, estoqueSaldoRepository, estoqueMovimentacaoService);
    }

    @Test
    void deveRejeitarFilialDeOutroTenantNaCriacao() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        when(filialRepository.existsByIdAndTenantId(filialId, tenantId)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service().criar(tenantId, null, filialId, "Inventario loja"));
        verifyNoInteractions(sessaoRepository);
    }

    @Test
    void deveRejeitarLimiteInvalidoAntesDosRepositorios() {
        assertThrows(RegraNegocioException.class,
                () -> service().listar(UUID.randomUUID(), null, null, 501));
        verifyNoInteractions(sessaoRepository, filialRepository);
    }
}
