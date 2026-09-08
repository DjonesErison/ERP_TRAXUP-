package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProdutoComboVigenciaApplicationService {
    private final ProdutoComboVigenciaRepository repository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoComboComponenteRepository componenteRepository;
    private final ProdutoComboGrupoRepository grupoRepository;

    public ProdutoComboVigenciaApplicationService(ProdutoComboVigenciaRepository repository,
                                                   ProdutoRepository produtoRepository,
                                                   ProdutoComboComponenteRepository componenteRepository,
                                                   ProdutoComboGrupoRepository grupoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.componenteRepository = componenteRepository;
        this.grupoRepository = grupoRepository;
    }

    public Optional<ProdutoComboVigencia> buscar(UUID tenantId, UUID comboProdutoId) {
        validarProduto(tenantId, comboProdutoId);
        return repository.findByTenantIdAndComboProdutoId(tenantId, comboProdutoId);
    }

    public boolean vigenteEm(UUID tenantId, UUID comboProdutoId, Instant instante) {
        return repository.findByTenantIdAndComboProdutoId(tenantId, comboProdutoId)
                .map(v -> v.vigenteEm(instante))
                .orElse(true);
    }

    @Transactional
    public ProdutoComboVigencia configurar(UUID tenantId, UUID comboProdutoId,
                                           Instant vigenciaInicio, Instant vigenciaFim) {
        validarProduto(tenantId, comboProdutoId);
        if (!componenteRepository.existsByTenantIdAndComboProdutoId(tenantId, comboProdutoId)
                && !grupoRepository.existsByTenantIdAndComboProdutoId(tenantId, comboProdutoId)) {
            throw new IllegalArgumentException("Vigencia so pode ser configurada para produto combo");
        }
        ProdutoComboVigencia vigencia = repository.findByTenantIdAndComboProdutoId(tenantId, comboProdutoId)
                .orElseGet(() -> new ProdutoComboVigencia(tenantId, comboProdutoId, vigenciaInicio, vigenciaFim));
        vigencia.definir(vigenciaInicio, vigenciaFim);
        return repository.save(vigencia);
    }

    @Transactional
    public void remover(UUID tenantId, UUID comboProdutoId) {
        validarProduto(tenantId, comboProdutoId);
        repository.deleteByTenantIdAndComboProdutoId(tenantId, comboProdutoId);
    }

    private void validarProduto(UUID tenantId, UUID produtoId) {
        produtoRepository.findByIdAndTenantId(produtoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o tenant informado"));
    }
}
