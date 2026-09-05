package com.traxup.tplug.erp.pessoa.contato.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPessoaContatoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 120) String cargo,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String telefone,
        boolean principal) {
}
