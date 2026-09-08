package com.traxup.tplug.erp.compra;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecebimentoCompraApplicationService {
    private final PedidoCompraRepository pedidoRepository;
    private final PedidoCompraItemRepository pedidoItemRepository;
    private final RecebimentoCompraRepository recebimentoRepository;
    private final RecebimentoCompraItemRepository recebimentoItemRepository;
    private final EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    private final AuditoriaApplicationService auditoria;

    public RecebimentoCompraApplicationService(PedidoCompraRepository pedidoRepository,
                                               PedidoCompraItemRepository pedidoItemRepository,
                                               RecebimentoCompraRepository recebimentoRepository,
                                               RecebimentoCompraItemRepository recebimentoItemRepository,
                                               EstoqueMovimentacaoApplicationService estoqueMovimentacaoService,
                                               AuditoriaApplicationService auditoria) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.recebimentoRepository = recebimentoRepository;
        this.recebimentoItemRepository = recebimentoItemRepository;
        this.estoqueMovimentacaoService = estoqueMovimentacaoService;
        this.auditoria = auditoria;
    }

    public List<RecebimentoCompra> listar(UUID tenantId) {
        return recebimentoRepository.findAllByTenantIdOrderByRecebidoEmDesc(tenantId);
    }

    public RecebimentoCompra buscar(UUID tenantId, UUID recebimentoId) {
        return recebimentoRepository.findByIdAndTenantId(recebimentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Recebimento de compra nao encontrado para o tenant informado"));
    }

    public List<RecebimentoCompraItem> listarItens(UUID tenantId, UUID recebimentoId) {
        buscar(tenantId, recebimentoId);
        return recebimentoItemRepository.findAllByTenantIdAndRecebimentoIdOrderByCriadoEmAsc(tenantId, recebimentoId);
    }

    @Transactional
    public RecebimentoCompra registrar(UUID tenantId, UUID usuarioId, UUID pedidoId, String documento,
                                       String observacao, Map<UUID, BigDecimal> quantidadesRecebidas) {
        PedidoCompra pedido = pedidoRepository.findByIdAndTenantIdForUpdate(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido de compra nao encontrado para o tenant informado"));
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new RegraNegocioException("Somente pedido ABERTO permite recebimento");
        }
        if (recebimentoRepository.existsByTenantIdAndPedidoCompraId(tenantId, pedidoId)) {
            throw new RecursoConflitanteException("Pedido de compra ja possui recebimento registrado");
        }

        List<PedidoCompraItem> itens = pedidoItemRepository
                .findAllByTenantIdAndPedidoCompraIdOrderByCriadoEmAsc(tenantId, pedidoId);
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Pedido de compra nao possui itens para recebimento");
        }

        Map<UUID, BigDecimal> qtds = quantidadesRecebidas == null ? Map.of() : new HashMap<>(quantidadesRecebidas);
        RecebimentoCompra recebimento = recebimentoRepository.save(new RecebimentoCompra(
                tenantId, pedidoId, pedido.getFilialId(), pedido.getFornecedorId(),
                normalizar(documento), normalizar(observacao), usuarioId));

        for (PedidoCompraItem item : itens) {
            BigDecimal quantidade = qtds.getOrDefault(item.getId(), item.getQuantidade());
            if (quantidade == null || quantidade.signum() < 0 || quantidade.compareTo(item.getQuantidade()) > 0) {
                throw new RegraNegocioException("Quantidade recebida invalida para o item " + item.getId());
            }
            recebimentoItemRepository.save(new RecebimentoCompraItem(tenantId, recebimento.getId(), item, quantidade));
        }

        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "CRIAR", "RECEBIMENTO_COMPRA", recebimento.getId(), "pedidoId=" + pedidoId);
        return recebimento;
    }

    @Transactional
    public RecebimentoCompra integrarEstoque(UUID tenantId, UUID usuarioId, UUID recebimentoId) {
        RecebimentoCompra recebimento = buscar(tenantId, recebimentoId);
        if (!"CONFERIDO".equals(recebimento.getStatus())) {
            throw new RecursoConflitanteException("Recebimento ja integrado ao estoque ou em estado invalido");
        }

        PedidoCompra pedido = pedidoRepository
                .findByIdAndTenantIdForUpdate(recebimento.getPedidoCompraId(), tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido de compra nao encontrado para o tenant informado"));
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new RegraNegocioException("Pedido precisa estar ABERTO para integrar o recebimento");
        }

        List<RecebimentoCompraItem> itens = recebimentoItemRepository
                .findAllByTenantIdAndRecebimentoIdOrderByCriadoEmAsc(tenantId, recebimentoId);
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Recebimento nao possui itens");
        }

        for (RecebimentoCompraItem item : itens) {
            if (item.getQuantidadeRecebida().signum() == 0) continue;
            String tipoItem = item.getGradeId() == null ? "PRODUTO" : "GRADE";
            UUID itemId = item.getGradeId() == null ? item.getProdutoId() : item.getGradeId();
            estoqueMovimentacaoService.movimentar(
                    tenantId, recebimento.getFilialId(), tipoItem, itemId, "ENTRADA",
                    item.getQuantidadeRecebida(), "Recebimento de compra " + recebimentoId, usuarioId);
        }

        recebimento.marcarIntegradoEstoque();
        recebimentoRepository.save(recebimento);
        pedido.marcarRecebido();
        pedidoRepository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, recebimento.getFilialId(),
                "INTEGRAR_ESTOQUE", "RECEBIMENTO_COMPRA", recebimento.getId(),
                "pedidoId=" + recebimento.getPedidoCompraId());
        return recebimento;
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
