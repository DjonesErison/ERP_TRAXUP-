package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InventarioLeituraApplicationService {
    private final ProdutoRepository produtoRepository;
    private final GradeProdutoRepository gradeProdutoRepository;

    public InventarioLeituraApplicationService(ProdutoRepository produtoRepository,
                                               GradeProdutoRepository gradeProdutoRepository) {
        this.produtoRepository = produtoRepository;
        this.gradeProdutoRepository = gradeProdutoRepository;
    }

    public InventarioItemLeitura localizarPorCodigoBarra(UUID tenantId, String codigoBarra) {
        if (codigoBarra == null || codigoBarra.isBlank()) {
            throw new RegraNegocioException("Codigo de barras e obrigatorio");
        }
        String codigo = codigoBarra.trim();
        if (codigo.length() > 60) {
            throw new RegraNegocioException("Codigo de barras deve possuir no maximo 60 caracteres");
        }

        List<GradeProduto> grades = gradeProdutoRepository
                .findAllByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, codigo);
        if (grades.size() > 1) {
            throw new RegraNegocioException("Codigo de barras ambiguo: mais de uma grade ativa encontrada");
        }
        if (grades.size() == 1) {
            GradeProduto grade = grades.getFirst();
            return new InventarioItemLeitura("GRADE", grade.getId(), grade.getCodigoGrade(),
                    grade.getDescricaoGrade(), grade.getCodigoBarra());
        }

        List<Produto> produtos = produtoRepository
                .findAllByTenantIdAndCodigoBarraAndAtivoTrue(tenantId, codigo);
        if (produtos.size() > 1) {
            throw new RegraNegocioException("Codigo de barras ambiguo: mais de um produto ativo encontrado");
        }
        if (produtos.isEmpty()) {
            throw new RecursoNaoEncontradoException("Item ativo nao encontrado para o codigo de barras informado");
        }

        Produto produto = produtos.getFirst();
        return new InventarioItemLeitura("PRODUTO", produto.getId(), produto.getCodigo(),
                produto.getDescricao(), produto.getCodigoBarra());
    }
}
