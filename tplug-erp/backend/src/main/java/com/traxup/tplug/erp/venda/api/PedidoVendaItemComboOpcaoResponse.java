package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.venda.PedidoVendaItemComboOpcao;

import java.util.UUID;

public record PedidoVendaItemComboOpcaoResponse(
        UUID id,
        UUID grupoId,
        UUID opcaoId
) {
    public static PedidoVendaItemComboOpcaoResponse from(PedidoVendaItemComboOpcao selecao) {
        return new PedidoVendaItemComboOpcaoResponse(selecao.getId(), selecao.getGrupoId(), selecao.getOpcaoId());
    }
}
