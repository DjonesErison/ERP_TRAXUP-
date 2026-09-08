package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.inventario.InventarioSessao;

import java.time.Instant;
import java.util.UUID;

public record InventarioSessaoResponse(
        UUID id,
        UUID filialId,
        String status,
        String descricao,
        boolean contagemCega,
        UUID criadoPorId,
        UUID concluidoPorId,
        UUID ajustadoPorId,
        Instant criadoEm,
        Instant atualizadoEm,
        Instant concluidoEm,
        Instant ajustadoEm
) {
    public static InventarioSessaoResponse from(InventarioSessao i) {
        return new InventarioSessaoResponse(
                i.getId(), i.getFilialId(), i.getStatus(), i.getDescricao(), i.isContagemCega(),
                i.getCriadoPorId(), i.getConcluidoPorId(), i.getAjustadoPorId(), i.getCriadoEm(),
                i.getAtualizadoEm(), i.getConcluidoEm(), i.getAjustadoEm());
    }
}
