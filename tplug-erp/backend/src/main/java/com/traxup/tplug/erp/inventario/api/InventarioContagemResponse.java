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
        return from(c, false);
    }

    public static InventarioContagemResponse from(InventarioContagem c, boolean ocultarSaldo) {
        return new InventarioContagemResponse(
                c.getId(), c.getTipoItem(), c.getItemId(),
                ocultarSaldo ? null : c.getQuantidadeSistema(),
                c.getQuantidadeContada(),
                ocultarSaldo ? null : c.getDivergencia(),
                c.getContadoPorId(), c.getContadoEm(), c.getAtualizadoEm());
    }
}
