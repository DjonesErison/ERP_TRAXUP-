package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InventarioDivergenciaApplicationService {
    private final InventarioApplicationService inventarioService;
    private final ProdutoRepository produtoRepository;
    private final GradeProdutoRepository gradeProdutoRepository;

    public InventarioDivergenciaApplicationService(InventarioApplicationService inventarioService,
                                                   ProdutoRepository produtoRepository,
                                                   GradeProdutoRepository gradeProdutoRepository) {
        this.inventarioService = inventarioService;
        this.produtoRepository = produtoRepository;
        this.gradeProdutoRepository = gradeProdutoRepository;
    }

    public List<InventarioDivergenciaItem> listar(UUID tenantId, UUID inventarioId, Integer limite) {
        return inventarioService.listarDivergencias(tenantId, inventarioId, limite)
                .stream().map(contagem -> enriquecer(tenantId, contagem)).toList();
    }

    private InventarioDivergenciaItem enriquecer(UUID tenantId, InventarioContagem contagem) {
        return switch (contagem.getTipoItem()) {
            case "PRODUTO" -> {
                Produto produto = produtoRepository.findByIdAndTenantId(contagem.getItemId(), tenantId)
                        .orElseThrow(() -> itemNaoEncontrado());
                yield new InventarioDivergenciaItem(contagem, produto.getCodigo(), produto.getDescricao());
            }
            case "GRADE" -> {
                GradeProduto grade = gradeProdutoRepository.findByIdAndTenantId(contagem.getItemId(), tenantId)
                        .orElseThrow(() -> itemNaoEncontrado());
                yield new InventarioDivergenciaItem(contagem, grade.getCodigoGrade(), grade.getDescricaoGrade());
            }
            default -> throw itemNaoEncontrado();
        };
    }

    private RecursoNaoEncontradoException itemNaoEncontrado() {
        return new RecursoNaoEncontradoException("Item da divergencia nao encontrado para o tenant informado");
    }
}
