package com.traxup.tplug.erp.produto.grade;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
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
public class GradeProdutoApplicationService {

    private final GradeProdutoRepository gradeRepository;
    private final ProdutoRepository produtoRepository;
    private final TenantRepository tenantRepository;

    public GradeProdutoApplicationService(GradeProdutoRepository gradeRepository,
                                          ProdutoRepository produtoRepository,
                                          TenantRepository tenantRepository) {
        this.gradeRepository = gradeRepository;
        this.produtoRepository = produtoRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<GradeProduto> listarPorProduto(UUID tenantId, UUID produtoId) {
        buscarProduto(tenantId, produtoId);
        return gradeRepository.findAllByTenantIdAndProdutoIdOrderByDescricaoGradeAsc(tenantId, produtoId);
    }

    public GradeProduto buscarPorId(UUID tenantId, UUID gradeId) {
        return gradeRepository.findByIdAndTenantId(gradeId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grade de produto nao encontrada para o tenant informado"));
    }

    @Transactional
    public GradeProduto criar(UUID tenantId, UUID produtoId, String codigoGrade, String descricaoGrade,
                              String codigoBarra, BigDecimal vendaPrc) {
        Tenant tenant = validarTenant(tenantId);
        Produto produto = buscarProduto(tenantId, produtoId);
        if (gradeRepository.existsByTenantIdAndCodigoGradeIgnoreCase(tenantId, codigoGrade)) {
            throw new RecursoConflitanteException("Ja existe grade com este codigo no tenant");
        }
        return gradeRepository.save(new GradeProduto(tenant, produto, codigoGrade, descricaoGrade, codigoBarra, vendaPrc));
    }

    @Transactional
    public GradeProduto desativar(UUID tenantId, UUID gradeId) {
        GradeProduto grade = buscarPorId(tenantId, gradeId);
        grade.desativar();
        return gradeRepository.save(grade);
    }

    private Produto buscarProduto(UUID tenantId, UUID produtoId) {
        return produtoRepository.findByIdAndTenantId(produtoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o tenant informado"));
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }
}
