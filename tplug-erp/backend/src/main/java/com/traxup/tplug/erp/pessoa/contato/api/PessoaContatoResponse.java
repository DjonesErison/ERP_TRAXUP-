package com.traxup.tplug.erp.pessoa.contato.api;

import com.traxup.tplug.erp.pessoa.contato.PessoaContato;

import java.time.Instant;
import java.util.UUID;

public record PessoaContatoResponse(
        UUID id, UUID pessoaId, String nome, String cargo, String email, String telefone,
        boolean principal, Instant criadoEm, Instant atualizadoEm) {

    public static PessoaContatoResponse from(PessoaContato contato) {
        return new PessoaContatoResponse(contato.getId(), contato.getPessoaId(), contato.getNome(), contato.getCargo(),
                contato.getEmail(), contato.getTelefone(), contato.isPrincipal(), contato.getCriadoEm(), contato.getAtualizadoEm());
    }
}
