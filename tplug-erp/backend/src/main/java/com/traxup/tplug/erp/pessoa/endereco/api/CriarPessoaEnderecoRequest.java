package com.traxup.tplug.erp.pessoa.endereco.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPessoaEnderecoRequest(
        @NotBlank String tipo,
        @NotBlank @Size(max = 255) String logradouro,
        @Size(max = 30) String numero,
        @Size(max = 120) String complemento,
        @Size(max = 120) String bairro,
        @NotBlank @Size(max = 120) String cidade,
        @NotBlank @Size(min = 2, max = 2) String uf,
        @Size(max = 10) String cep,
        boolean principal) {
}
