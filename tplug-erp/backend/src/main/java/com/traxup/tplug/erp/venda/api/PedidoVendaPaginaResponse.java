package com.traxup.tplug.erp.venda.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record PedidoVendaPaginaResponse(
        List<PedidoVendaResponse> conteudo,
        int pagina,
        int tamanho,
        long totalRegistros,
        int totalPaginas
) {
    public static PedidoVendaPaginaResponse from(Page<com.traxup.tplug.erp.venda.PedidoVenda> pagina) {
        return new PedidoVendaPaginaResponse(
                pagina.getContent().stream().map(PedidoVendaResponse::from).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
