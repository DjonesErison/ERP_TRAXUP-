package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ImportarConciliacaoLoteRequest(
        @NotEmpty @Size(max = 500) List<@Valid ImportarConciliacaoLancamentoRequest> lancamentos
) {}
