package com.traxup.tplug.erp.filial.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarFilialRequest(
        @NotNull UUID empresaId,
        @NotBlank @Size(max = 200) String nome,
        @Size(max = 14) String cnpj) {
}
