package com.traxup.tplug.erp.fiscal.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarFiscalSolicitacaoRequest(
        @NotNull UUID pedidoVendaId,
        @NotBlank String modelo,
        @NotBlank String ambiente
) {}
