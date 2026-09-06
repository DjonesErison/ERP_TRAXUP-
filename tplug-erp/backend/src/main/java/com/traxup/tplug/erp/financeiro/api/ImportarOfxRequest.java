package com.traxup.tplug.erp.financeiro.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImportarOfxRequest(
        @NotBlank @Size(max = 5000000) String conteudo
) {}
