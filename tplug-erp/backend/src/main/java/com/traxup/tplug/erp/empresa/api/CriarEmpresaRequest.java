package com.traxup.tplug.erp.empresa.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarEmpresaRequest(
        @NotBlank(message = "razaoSocial e obrigatoria")
        @Size(max = 200, message = "razaoSocial deve ter no maximo 200 caracteres")
        String razaoSocial,

        @Size(max = 200, message = "nomeFantasia deve ter no maximo 200 caracteres")
        String nomeFantasia,

        @Pattern(regexp = "^$|\\d{14}$", message = "cnpj deve conter 14 digitos")
        String cnpj) {
}
