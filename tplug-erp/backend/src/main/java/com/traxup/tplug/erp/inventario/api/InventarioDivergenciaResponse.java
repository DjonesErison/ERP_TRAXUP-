package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.inventario.InventarioContagem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventarioDivergenciaResponse(
        UUID id,
        String tipoItem,
        UUID itemId,
        String codigoItem,
        String descricaoItem,
        BigDecimal quantidadeSistema,
        BigDecimal quantidadeContada,
        BigDecimal divergencia,
        UUID contadoPorId,
        Instant contadoEm,
        Instant atualizadoEm
) {
    public static InventarioDivergenciaResponse from(InventarioContagem c, String codigoItem, String descricaoItem) {
        return new InventarioDivergenciaResponse(c.getId(), c.getTipoItem(), c.getItemId(), codigoItem, descricaoItem,
                c.getQuantidadeSistema(), c.getQuantidadeContada(), c.getDivergencia(), c.getContadoPorId(),
                c.getContadoEm(), c.getAtualizadoEm());
    }
}
