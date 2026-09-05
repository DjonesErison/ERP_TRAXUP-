package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoCompraItemApplicationService {

    private final PedidoCompraRepository pedidoRepository;
    private final PedidoCompraItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final GradeProdutoRepository gradeProdutoRepository;
    private final AuditoriaApplicationService auditoria;

    public PedidoCompraItemApplicationService(PedidoCompraRepository pedidoRepository,
                                               PedidoCompraItemRepository itemRepository,
                                               ProdutoRepository produtoRepository,
                                               GradeProdutoRepository gradeProdutoRepository,
                                               AuditoriaApplicationService auditoria) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.gradeProdutoRepository = gradeProdutoRepository;
        this.auditoria = auditoria;
    }

    public List<PedidoCompraItem> listar(UUID tenantId, UUID pedidoId) {
        buscarPedido(tenantId, pedidoId, false);
        return itemRepository.findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId);
    }

    @Transactional
    public PedidoCompraItem adicionar(UUID tenantId, UUID usuarioId, UUID pedidoId, UUID produtoId, UUID gradeId,
                                      BigDecimal quantidade, BigDecimal precoUnitario) {
        PedidoCompra pedido = buscarPedido(tenantId, pedidoId, true);
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (precoUnitario == null || precoUnitario.signum() < 0) {
            throw new IllegalArgumentException("Preco unitario nao pode ser negativo");
        }

        Produto produto = produtoRepository.findByIdAndTenantId(produtoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o tenant informado"));
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("Produto informado esta inativo");
        }

        if (gradeId != null) {
            GradeProduto grade = gradeProdutoRepository.findByIdAndTenantId(gradeId, tenantId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Grade nao encontrada para o tenant informado"));
            if (!grade.isAtivo()) {
                throw new IllegalArgumentException("Grade informada esta inativa");
            }
            if (!grade.getProduto().getId().equals(produtoId)) {
                throw new IllegalArgumentException("Grade informada nao pertence ao produto do item");
            }
        }

        PedidoCompraItem item = itemRepository.save(
                new PedidoCompraItem(tenantId, pedidoId, produtoId, gradeId, quantidade, precoUnitario));

        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "CRIAR", "PEDIDO_COMPRA_ITEM", item.getId(),
                "pedidoId=" + pedidoId + ";produtoId=" + produtoId + ";gradeId=" + gradeId);
        return item;
    }

    private PedidoCompra buscarPedido(UUID tenantId, UUID pedidoId, boolean exigirRascunho) {
        PedidoCompra pedido = pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra nao encontrado para o tenant informado"));
        if (exigirRascunho && !"RASCUNHO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Itens so podem ser alterados enquanto o pedido estiver em RASCUNHO");
        }
        return pedido;
    }
}
