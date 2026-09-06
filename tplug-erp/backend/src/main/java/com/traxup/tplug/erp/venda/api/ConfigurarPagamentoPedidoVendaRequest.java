package com.traxup.tplug.erp.venda.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfigurarPagamentoPedidoVendaRequest(
        @NotNull UUID formaPagamentoId,
        @NotNull UUID condicaoPagamentoId
) {}
