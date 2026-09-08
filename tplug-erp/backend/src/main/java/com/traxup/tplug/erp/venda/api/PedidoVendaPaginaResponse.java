package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVenda;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PedidoVendaPaginaResponse(
        List<PedidoVendaRecenteResponse> conteudo,
        int pagina,
        int tamanho,
        long totalRegistros,
        int totalPaginas
) {
    public static PedidoVendaPaginaResponse from(Page<PedidoVenda> pagina, Map<UUID, BigDecimal> totais) {
        return new PedidoVendaPaginaResponse(
                pagina.getContent().stream()
                        .map(pedido -> PedidoVendaRecenteResponse.from(pedido, totais.get(pedido.getId())))
                        .toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
