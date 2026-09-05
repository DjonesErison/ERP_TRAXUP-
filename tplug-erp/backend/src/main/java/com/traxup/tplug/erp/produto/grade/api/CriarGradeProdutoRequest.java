package com.traxup.tplug.erp.produto.grade.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarGradeProdutoRequest(
        @NotBlank @Size(max = 60) String codigoGrade,
        @NotBlank @Size(max = 255) String descricaoGrade,
        @Size(max = 60) String codigoBarra,
        @PositiveOrZero BigDecimal vendaPrc) {
}
