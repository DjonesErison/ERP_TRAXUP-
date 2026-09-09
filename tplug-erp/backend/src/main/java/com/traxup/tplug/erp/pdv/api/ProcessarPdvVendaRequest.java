package com.traxup.tplug.erp.pdv.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProcessarPdvVendaRequest(
        @NotNull UUID terminalId,
        @NotNull UUID operacaoLocalId,
        @NotNull @Positive Long numeroLocal,
        @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{64}$") String checksum,
        @NotNull Instant ocorridoEm,
        UUID clienteId,
        @NotNull UUID formaPagamentoId,
        @NotNull UUID condicaoPagamentoId,
        @Size(max = 500) String observacao,
        @NotEmpty List<@Valid PdvVendaItemSincronizacaoRequest> itens
) {}
