package com.traxup.tplug.erp.fiscal.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record SalvarFiscalCertificadoRequest(
        @NotBlank @Size(max = 5) String tipo,
        @NotBlank @Size(max = 255) String titular,
        @NotBlank @Size(max = 18) String documentoTitular,
        @NotBlank @Size(max = 120) String numeroSerie,
        @NotBlank @Size(max = 64) String thumbprintSha256,
        @NotNull OffsetDateTime validadeInicio,
        @NotNull OffsetDateTime validadeFim,
        @NotBlank @Size(max = 60) String cofreSegredos,
        @NotBlank @Size(max = 500) String referenciaSegredo
) {}
