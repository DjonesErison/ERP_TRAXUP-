package com.traxup.tplug.erp.produto.combo.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdicionarProdutoComboGrupoOpcaoRequest(
        @NotNull UUID produtoId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal quantidade,
        @DecimalMin(value = "0.0000") BigDecimal valorAdicional
) {}
