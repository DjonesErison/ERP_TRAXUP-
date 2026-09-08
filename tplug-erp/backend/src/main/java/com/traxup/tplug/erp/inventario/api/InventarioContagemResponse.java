package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.inventario.InventarioContagem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventarioContagemResponse(
        UUID id,
        String tipoItem,
        UUID itemId,
        BigDecimal quantidadeSistema,
        BigDecimal quantidadeContada,
        BigDecimal divergencia,
        UUID contadoPorId,
        Instant contadoEm,
        Instant atualizadoEm
) {
    public static InventarioContagemResponse from(InventarioContagem c) {
        return new InventarioContagemResponse(c.getId(), c.getTipoItem(), c.getItemId(), c.getQuantidadeSistema(),
                c.getQuantidadeContada(), c.getDivergencia(), c.getContadoPorId(), c.getContadoEm(), c.getAtualizadoEm());
    }
}
