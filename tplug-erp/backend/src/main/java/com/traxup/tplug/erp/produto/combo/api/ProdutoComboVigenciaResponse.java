package com.traxup.tplug.erp.produto.combo.api;

import com.traxup.tplug.erp.produto.combo.ProdutoComboVigencia;

import java.time.Instant;
import java.util.UUID;

public record ProdutoComboVigenciaResponse(
        UUID produtoId,
        Instant vigenciaInicio,
        Instant vigenciaFim
) {
    public static ProdutoComboVigenciaResponse from(ProdutoComboVigencia vigencia) {
        return new ProdutoComboVigenciaResponse(
                vigencia.getComboProdutoId(),
                vigencia.getVigenciaInicio(),
                vigencia.getVigenciaFim());
    }
}
