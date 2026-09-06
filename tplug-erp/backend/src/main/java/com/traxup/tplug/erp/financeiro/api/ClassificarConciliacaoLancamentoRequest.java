package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassificarConciliacaoLancamentoRequest(
        @NotBlank @Size(max = 20) String natureza
) {}
