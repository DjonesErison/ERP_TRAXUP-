package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.combo.ProdutoComboVigenciaApplicationService;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaItemApplicationService {

    private final PedidoVendaRepository pedidoRepository;
    private final PedidoVendaItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final GradeProdutoRepository gradeProdutoRepository;
    private final ProdutoComboVigenciaApplicationService comboVigenciaService;
    private final AuditoriaApplicationService auditoria;

    public PedidoVendaItemApplicationService(PedidoVendaRepository pedidoRepository,
                                              PedidoVendaItemRepository itemRepository,
                                              ProdutoRepository produtoRepository,
                                              GradeProdutoRepository gradeProdutoRepository,
                                              ProdutoComboVigenciaApplicationService comboVigenciaService,
                                              AuditoriaApplicationService auditoria) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.gradeProdutoRepository = gradeProdutoRepository;
        this.comboVigenciaService = comboVigenciaService;
        this.auditoria = auditoria;
    }

    public List<PedidoVendaItem> listar(UUID tenantId, UUID pedidoId) {
        buscarPedido(tenantId, pedidoId, false);
        return itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
    }

    @Transactional
    public PedidoVendaItem adicionar(UUID tenantId, UUID usuarioId, UUID pedidoId, UUID produtoId, UUID gradeId,
                                     BigDecimal quantidade, BigDecimal precoUnitario) {
        PedidoVenda pedido = buscarPedidoParaMutacao(tenantId, pedidoId);
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
        if (!comboVigenciaService.vigenteEm(tenantId, produtoId, Instant.now())) {
            throw new IllegalArgumentException("Produto combo esta fora da vigencia para novas vendas");
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

        PedidoVendaItem item = itemRepository.save(
                new PedidoVendaItem(tenantId, pedidoId, produtoId, gradeId, quantidade, precoUnitario));

        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "CRIAR", "PEDIDO_VENDA_ITEM", item.getId(),
                "pedidoId=" + pedidoId + ";produtoId=" + produtoId + ";gradeId=" + gradeId);
        return item;
    }

    @Transactional
    public PedidoVendaItem aplicarDesconto(UUID tenantId, UUID usuarioId, UUID pedidoId, UUID itemId, BigDecimal descontoValor) {
        PedidoVenda pedido = buscarPedidoParaMutacao(tenantId, pedidoId);
        PedidoVendaItem item = itemRepository.findByIdAndTenantIdAndPedidoVendaId(itemId, tenantId, pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do pedido de venda nao encontrado para o tenant informado"));
        item.aplicarDesconto(descontoValor);
        PedidoVendaItem salvo = itemRepository.save(item);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "ALTERAR", "PEDIDO_VENDA_ITEM", item.getId(),
                "pedidoId=" + pedidoId + ";descontoValor=" + descontoValor);
        return salvo;
    }

    private PedidoVenda buscarPedido(UUID tenantId, UUID pedidoId, boolean exigirRascunho) {
        PedidoVenda pedido = pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
        validarRascunhoSeNecessario(pedido, exigirRascunho);
        return pedido;
    }

    private PedidoVenda buscarPedidoParaMutacao(UUID tenantId, UUID pedidoId) {
        PedidoVenda pedido = pedidoRepository.buscarParaAtualizar(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
        validarRascunhoSeNecessario(pedido, true);
        return pedido;
    }

    private void validarRascunhoSeNecessario(PedidoVenda pedido, boolean exigirRascunho) {
        if (exigirRascunho && !"RASCUNHO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Itens so podem ser alterados enquanto o pedido estiver em RASCUNHO");
        }
    }
}
