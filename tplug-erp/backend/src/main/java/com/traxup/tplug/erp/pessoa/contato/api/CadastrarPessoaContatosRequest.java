package com.traxup.tplug.erp.pessoa.contato.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CadastrarPessoaContatosRequest(
        @NotEmpty List<@Valid CriarPessoaContatoRequest> contatos) {
}
