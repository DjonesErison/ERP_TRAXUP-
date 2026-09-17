package com.traxup.tplug.erp.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecuperacaoSenhaConfirmarRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 128) String novaSenha
) {
}
