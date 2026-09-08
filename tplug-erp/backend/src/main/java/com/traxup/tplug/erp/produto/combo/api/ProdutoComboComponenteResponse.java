package com.traxup.tplug.erp.produto.combo.api;

import com.traxup.tplug.erp.produto.combo.ProdutoComboComponente;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProdutoComboComponenteResponse(
        UUID id,
        UUID produtoId,
        BigDecimal quantidade,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ProdutoComboComponenteResponse from(ProdutoComboComponente componente) {
        return new ProdutoComboComponenteResponse(
                componente.getId(),
                componente.getComponenteProdutoId(),
                componente.getQuantidade(),
                componente.getCriadoEm(),
                componente.getAtualizadoEm());
    }
}
