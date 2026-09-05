package com.traxup.tplug.erp.venda.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarPedidoVendaRequest(
        @NotNull UUID filialId,
        UUID clienteId,
        @NotBlank @Size(max = 40) String numero,
        @Size(max = 500) String observacao
) {}
