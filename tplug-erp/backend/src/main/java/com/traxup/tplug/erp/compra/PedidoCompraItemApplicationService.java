package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoCompraItemApplicationService {

    private final PedidoCompraRepository pedidoRepository;
    private final PedidoCompraItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final GradeProdutoRepository gradeProdutoRepository;

    public PedidoCompraItemApplicationService(PedidoCompraRepository pedidoRepository,
                                               PedidoCompraItemRepository itemRepository,
                                               ProdutoRepository produtoRepository,
                                               GradeProdutoRepository gradeProdutoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.gradeProdutoRepository = gradeProdutoRepository;
    }

    public List<PedidoCompraItem> listar(UUID tenantId, UUID pedidoId) {
        buscarPedidoRascunhoOuExistente(tenantId, pedidoId, false);
        return itemRepository.findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId);
    }

    @Transactional
    public PedidoCompraItem adicionar(UUID tenantId, UUID pedidoId, String tipoItem, UUID itemId,
                                      BigDecimal quantidade, BigDecimal precoUnitario) {
        buscarPedidoRascunhoOuExistente(tenantId, pedidoId, true);

        String tipo = tipoItem == null ? "" : tipoItem.trim().toUpperCase(Locale.ROOT);
        if (!tipo.equals("PRODUTO") && !tipo.equals("GRADE")) {
            throw new IllegalArgumentException("Tipo de item deve ser PRODUTO ou GRADE");
        }
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (precoUnitario == null || precoUnitario.signum() < 0) {
            throw new IllegalArgumentException("Preco unitario nao pode ser negativo");
        }

        boolean existe = tipo.equals("PRODUTO")
                ? produtoRepository.findByIdAndTenantId(itemId, tenantId).isPresent()
                : gradeProdutoRepository.findByIdAndTenantId(itemId, tenantId).isPresent();
        if (!existe) {
            throw new RecursoNaoEncontradoException("Item de compra nao encontrado para o tenant informado");
        }

        return itemRepository.save(new PedidoCompraItem(tenantId, pedidoId, tipo, itemId, quantidade, precoUnitario));
    }

    private PedidoCompra buscarPedidoRascunhoOuExistente(UUID tenantId, UUID pedidoId, boolean exigirRascunho) {
        PedidoCompra pedido = pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra nao encontrado para o tenant informado"));
        if (exigirRascunho && !"RASCUNHO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Itens so podem ser alterados enquanto o pedido estiver em RASCUNHO");
        }
        return pedido;
    }
}
