package com.traxup.tplug.erp.pessoa.api;

import com.traxup.tplug.erp.pessoa.Pessoa;

import java.time.Instant;
import java.util.UUID;

public record PessoaResponse(
        UUID id,
        String tipoPessoa,
        String nomeRazaoSocial,
        String nomeFantasia,
        String cpfCnpj,
        String email,
        String telefone,
        boolean cliente,
        boolean fornecedor,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static PessoaResponse from(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(), pessoa.getTipoPessoa(), pessoa.getNomeRazaoSocial(), pessoa.getNomeFantasia(),
                pessoa.getCpfCnpj(), pessoa.getEmail(), pessoa.getTelefone(), pessoa.isCliente(),
                pessoa.isFornecedor(), pessoa.isAtivo(), pessoa.getCriadoEm(), pessoa.getAtualizadoEm());
    }
}
