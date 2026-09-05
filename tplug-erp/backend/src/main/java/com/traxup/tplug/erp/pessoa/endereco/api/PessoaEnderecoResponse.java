package com.traxup.tplug.erp.pessoa.endereco.api;

import com.traxup.tplug.erp.pessoa.endereco.PessoaEndereco;

import java.time.Instant;
import java.util.UUID;

public record PessoaEnderecoResponse(
        UUID id,
        UUID pessoaId,
        String tipo,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep,
        boolean principal,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static PessoaEnderecoResponse from(PessoaEndereco endereco) {
        return new PessoaEnderecoResponse(
                endereco.getId(), endereco.getPessoaId(), endereco.getTipo(), endereco.getLogradouro(),
                endereco.getNumero(), endereco.getComplemento(), endereco.getBairro(), endereco.getCidade(),
                endereco.getUf(), endereco.getCep(), endereco.isPrincipal(), endereco.getCriadoEm(), endereco.getAtualizadoEm());
    }
}
