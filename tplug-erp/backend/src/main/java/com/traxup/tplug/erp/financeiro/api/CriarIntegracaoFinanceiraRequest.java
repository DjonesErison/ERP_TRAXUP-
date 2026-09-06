package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarIntegracaoFinanceiraRequest(
        @NotBlank @Size(max = 40) String provedor,
        @Size(max = 120) String identificadorExterno
) {}
