package com.traxup.tplug.erp.produto.combo.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ConfigurarProdutoComboRequest(
        @NotEmpty List<@Valid Componente> componentes
) {
    public record Componente(
            @NotNull UUID produtoId,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal quantidade
    ) {}
}
