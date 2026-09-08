package com.traxup.tplug.erp.venda.api;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ConfigurarPedidoVendaItemComboRequest(
        @NotNull List<@NotNull UUID> opcaoIds
) {}
