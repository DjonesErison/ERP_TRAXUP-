package com.traxup.tplug.erp.produto;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProdutoApplicationService {

    private final ProdutoRepository produtoRepository;
    private final TenantRepository tenantRepository;

    public ProdutoApplicationService(ProdutoRepository produtoRepository, TenantRepository tenantRepository) {
        this.produtoRepository = produtoRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<Produto> listar(UUID tenantId) {
        validarTenant(tenantId);
        return produtoRepository.findAllByTenantIdOrderByDescricaoAsc(tenantId);
    }

    public Produto buscarPorId(UUID tenantId, UUID produtoId) {
        return produtoRepository.findByIdAndTenantId(produtoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o tenant informado"));
    }

    @Transactional
    public Produto criar(UUID tenantId, String codigo, String descricao, String grupo, String ncm,
                         BigDecimal vendaPrc, BigDecimal compraPrc, String codigoBarra, String unidade) {
        Tenant tenant = validarTenant(tenantId);
        if (produtoRepository.existsByTenantIdAndCodigoIgnoreCase(tenantId, codigo)) {
            throw new RecursoConflitanteException("Ja existe produto com este codigo no tenant");
        }
        Produto produto = new Produto(tenant, codigo, descricao, grupo, ncm, vendaPrc, compraPrc, codigoBarra, unidade);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto desativar(UUID tenantId, UUID produtoId) {
        Produto produto = buscarPorId(tenantId, produtoId);
        produto.desativar();
        return produtoRepository.save(produto);
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }
}
