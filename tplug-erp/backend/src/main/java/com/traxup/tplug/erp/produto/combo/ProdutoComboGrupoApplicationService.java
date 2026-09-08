package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProdutoComboGrupoApplicationService {
    private final ProdutoComboGrupoRepository grupoRepository;
    private final ProdutoComboGrupoOpcaoRepository opcaoRepository;
    private final ProdutoRepository produtoRepository;

    public ProdutoComboGrupoApplicationService(ProdutoComboGrupoRepository grupoRepository,
                                                ProdutoComboGrupoOpcaoRepository opcaoRepository,
                                                ProdutoRepository produtoRepository) {
        this.grupoRepository = grupoRepository;
        this.opcaoRepository = opcaoRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<ProdutoComboGrupo> listarGrupos(UUID tenantId, UUID comboProdutoId) {
        validarProduto(tenantId, comboProdutoId, "Combo");
        return grupoRepository.findAllByTenantIdAndComboProdutoIdOrderByNomeAsc(tenantId, comboProdutoId);
    }

    public List<ProdutoComboGrupoOpcao> listarOpcoes(UUID tenantId, UUID comboProdutoId, UUID grupoId) {
        buscarGrupo(tenantId, comboProdutoId, grupoId);
        return opcaoRepository.findAllByTenantIdAndGrupoIdOrderByProdutoIdAsc(tenantId, grupoId);
    }

    @Transactional
    public ProdutoComboGrupo criarGrupo(UUID tenantId, UUID comboProdutoId, String nome, int minimo, int maximo) {
        validarProduto(tenantId, comboProdutoId, "Combo");
        String nomeNormalizado = nome == null ? "" : nome.trim();
        if (nomeNormalizado.isEmpty()) throw new IllegalArgumentException("Nome do grupo e obrigatorio");
        if (minimo < 0 || maximo < 1 || minimo > maximo) throw new IllegalArgumentException("Intervalo de escolhas invalido");
        if (grupoRepository.existsByTenantIdAndComboProdutoIdAndNomeIgnoreCase(tenantId, comboProdutoId, nomeNormalizado)) {
            throw new IllegalArgumentException("Grupo de escolha ja cadastrado para o combo");
        }
        return grupoRepository.save(new ProdutoComboGrupo(tenantId, comboProdutoId, nomeNormalizado, minimo, maximo));
    }

    @Transactional
    public ProdutoComboGrupoOpcao adicionarOpcao(UUID tenantId, UUID comboProdutoId, UUID grupoId,
                                                  UUID produtoId, BigDecimal quantidade, BigDecimal valorAdicional) {
        buscarGrupo(tenantId, comboProdutoId, grupoId);
        validarProduto(tenantId, produtoId, "Produto da opcao");
        if (comboProdutoId.equals(produtoId)) throw new IllegalArgumentException("Combo nao pode ser opcao de si mesmo");
        if (quantidade == null || quantidade.signum() <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        BigDecimal adicional = valorAdicional == null ? BigDecimal.ZERO : valorAdicional;
        if (adicional.signum() < 0) throw new IllegalArgumentException("Valor adicional nao pode ser negativo");
        if (opcaoRepository.existsByTenantIdAndGrupoIdAndProdutoId(tenantId, grupoId, produtoId)) {
            throw new IllegalArgumentException("Produto ja cadastrado como opcao do grupo");
        }
        return opcaoRepository.save(new ProdutoComboGrupoOpcao(tenantId, grupoId, produtoId, quantidade, adicional));
    }

    private ProdutoComboGrupo buscarGrupo(UUID tenantId, UUID comboProdutoId, UUID grupoId) {
        return grupoRepository.findByIdAndTenantIdAndComboProdutoId(grupoId, tenantId, comboProdutoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo de escolha nao encontrado para o tenant e combo informados"));
    }

    private void validarProduto(UUID tenantId, UUID produtoId, String rotulo) {
        produtoRepository.findByIdAndTenantId(produtoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(rotulo + " nao encontrado para o tenant informado"));
    }
}
