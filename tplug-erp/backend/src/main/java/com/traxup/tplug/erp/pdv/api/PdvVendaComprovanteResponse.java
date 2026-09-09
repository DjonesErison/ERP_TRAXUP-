package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.pdv.PdvPosVendaApplicationService;
import com.traxup.tplug.erp.venda.api.PedidoVendaDetalheResponse;

import java.time.Instant;

public record PdvVendaComprovanteResponse(
        PedidoVendaDetalheResponse venda,
        Instant emitidoEm,
        String tipo
) {
    public static PdvVendaComprovanteResponse from(PdvPosVendaApplicationService.Comprovante comprovante) {
        var detalhe = comprovante.detalhe();
        return new PdvVendaComprovanteResponse(
                PedidoVendaDetalheResponse.from(detalhe.pedido(), detalhe.itens(), detalhe.comboOpcoes()),
                comprovante.emitidoEm(),
                "SEGUNDA_VIA");
    }
}
