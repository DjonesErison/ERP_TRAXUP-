package com.traxup.tplug.erp.pdv.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarPdvTerminalRequest(
        @NotNull UUID filialId,
        @NotBlank @Size(max = 64) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @NotNull @Min(1) Integer serie
) {}
