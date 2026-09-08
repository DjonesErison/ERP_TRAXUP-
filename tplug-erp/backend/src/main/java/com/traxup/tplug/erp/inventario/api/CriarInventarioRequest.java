package com.traxup.tplug.erp.inventario.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarInventarioRequest(
        @NotNull UUID filialId,
        @Size(max = 160) String descricao
) {}
