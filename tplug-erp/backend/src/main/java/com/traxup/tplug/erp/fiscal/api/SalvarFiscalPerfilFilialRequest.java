package com.traxup.tplug.erp.fiscal.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SalvarFiscalPerfilFilialRequest(
        @NotBlank String regimeTributario,
        @NotNull @Min(1) @Max(3) Short crt,
        @NotBlank String ambiente,
        @NotNull @Min(1) @Max(999) Integer serieNfe,
        @NotNull @Min(1) @Max(999) Integer serieNfce
) {}
