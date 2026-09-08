package com.traxup.tplug.erp.produto.combo.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarProdutoComboGrupoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Min(0) @Max(100) int minimoEscolhas,
        @Min(1) @Max(100) int maximoEscolhas
) {}
