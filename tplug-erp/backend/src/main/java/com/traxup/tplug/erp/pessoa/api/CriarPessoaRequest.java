package com.traxup.tplug.erp.pessoa.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPessoaRequest(
        @NotBlank String tipoPessoa,
        @NotBlank @Size(max = 255) String nomeRazaoSocial,
        @Size(max = 255) String nomeFantasia,
        @Size(max = 18) String cpfCnpj,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String telefone,
        boolean cliente,
        boolean fornecedor) {
}
