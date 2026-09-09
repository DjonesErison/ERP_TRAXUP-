package com.traxup.tplug.erp.pdv.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FecharPdvVendaRequest(
        @NotNull UUID formaPagamentoId,
        @NotNull UUID condicaoPagamentoId
) {}
