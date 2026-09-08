package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProdutoComboApplicationService {
    private final ProdutoComboComponenteRepository comboRepository;
    private final ProdutoRepository produtoRepository;

    public ProdutoComboApplicationService(ProdutoComboComponenteRepository comboRepository,
                                          ProdutoRepository produtoRepository) {
        this.comboRepository = comboRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<ProdutoComboComponente> listar(UUID tenantId, UUID comboProdutoId) {
        buscarProduto(tenantId, comboProdutoId);
        return comboRepository.findAllByTenantIdAndComboProdutoIdOrderByComponenteProdutoIdAsc(tenantId, comboProdutoId);
    }

    @Transactional
    public List<ProdutoComboComponente> configurar(UUID tenantId, UUID comboProdutoId,
                                                   List<ComponenteConfig> componentes) {
        Produto combo = buscarProdutoAtivo(tenantId, comboProdutoId, "Produto do combo");
        if (componentes == null || componentes.isEmpty()) {
            throw new IllegalArgumentException("Combo fixo precisa possuir ao menos um componente");
        }
        if (comboRepository.existsByTenantIdAndComponenteProdutoId(tenantId, combo.getId())) {
            throw new IllegalArgumentException("Produto usado como componente nao pode ser configurado como combo fixo");
        }

        Set<UUID> ids = new HashSet<>();
        List<ProdutoComboComponente> novos = componentes.stream().map(config -> {
            if (config == null || config.produtoId() == null) {
                throw new IllegalArgumentException("Produto componente e obrigatorio");
            }
            if (config.quantidade() == null || config.quantidade().signum() <= 0) {
                throw new IllegalArgumentException("Quantidade do componente deve ser maior que zero");
            }
            if (comboProdutoId.equals(config.produtoId())) {
                throw new IllegalArgumentException("Combo nao pode conter o proprio produto");
            }
            if (!ids.add(config.produtoId())) {
                throw new IllegalArgumentException("Produto componente duplicado no combo");
            }

            Produto componente = buscarProdutoAtivo(tenantId, config.produtoId(), "Produto componente");
            if (comboRepository.existsByTenantIdAndComboProdutoId(tenantId, componente.getId())) {
                throw new IllegalArgumentException("Combo fixo nao pode conter outro combo");
            }
            return new ProdutoComboComponente(tenantId, comboProdutoId, componente.getId(), config.quantidade());
        }).toList();

        comboRepository.deleteAllByTenantIdAndComboProdutoId(tenantId, comboProdutoId);
        return comboRepository.saveAll(novos).stream()
                .sorted(java.util.Comparator.comparing(ProdutoComboComponente::getComponenteProdutoId))
                .toList();
    }

    @Transactional
    public void remover(UUID tenantId, UUID comboProdutoId) {
        buscarProduto(tenantId, comboProdutoId);
        comboRepository.deleteAllByTenantIdAndComboProdutoId(tenantId, comboProdutoId);
    }

    private Produto buscarProdutoAtivo(UUID tenantId, UUID produtoId, String rotulo) {
        Produto produto = buscarProduto(tenantId, produtoId);
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(rotulo + " deve estar ativo");
        }
        return produto;
    }

    private Produto buscarProduto(UUID tenantId, UUID produtoId) {
        return produtoRepository.findByIdAndTenantId(produtoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o tenant informado"));
    }

    public record ComponenteConfig(UUID produtoId, BigDecimal quantidade) {}
}
