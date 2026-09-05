package com.traxup.tplug.erp.compra.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarPedidoCompraRequest(
        @NotNull UUID filialId,
        @NotNull UUID fornecedorId,
        @NotBlank @Size(max = 40) String numero,
        @Size(max = 500) String observacao) {
}
