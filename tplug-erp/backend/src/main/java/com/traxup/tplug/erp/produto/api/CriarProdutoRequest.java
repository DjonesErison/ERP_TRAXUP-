package com.traxup.tplug.erp.produto.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarProdutoRequest(
        @NotBlank @Size(max = 60) String codigo,
        @NotBlank @Size(max = 255) String descricao,
        @Size(max = 120) String grupo,
        @Size(max = 8) String ncm,
        @NotNull @DecimalMin("0.0000") BigDecimal vendaPrc,
        @NotNull @DecimalMin("0.0000") BigDecimal compraPrc,
        @Size(max = 60) String codigoBarra,
        @NotBlank @Size(max = 10) String unidade) {
}
