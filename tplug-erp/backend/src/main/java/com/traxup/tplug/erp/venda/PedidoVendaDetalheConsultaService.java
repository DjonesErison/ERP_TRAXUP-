package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PedidoVendaDetalheConsultaService {
    private final PedidoVendaRepository pedidoRepository;
    private final PedidoVendaItemRepository itemRepository;

    public PedidoVendaDetalheConsultaService(PedidoVendaRepository pedidoRepository,
                                             PedidoVendaItemRepository itemRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
    }

    public Detalhe consultar(UUID tenantId, UUID pedidoId) {
        PedidoVenda pedido = pedidoRepository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido de venda nao encontrado para o tenant informado"));
        List<PedidoVendaItem> itens = itemRepository
                .findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
        return new Detalhe(pedido, itens);
    }

    public Map<UUID, BigDecimal> totalLiquidoPorPedidos(UUID tenantId, List<UUID> pedidoIds) {
        if (pedidoIds == null || pedidoIds.isEmpty()) {
            return Map.of();
        }
        return itemRepository.somarTotaisPorPedidos(tenantId, pedidoIds).stream()
                .collect(Collectors.toMap(PedidoVendaItemRepository.TotalPedido::getPedidoVendaId,
                        PedidoVendaItemRepository.TotalPedido::getTotalLiquido));
    }

    public record Detalhe(PedidoVenda pedido, List<PedidoVendaItem> itens) {}
}
