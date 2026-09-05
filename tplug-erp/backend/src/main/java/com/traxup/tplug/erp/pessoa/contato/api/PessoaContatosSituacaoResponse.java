package com.traxup.tplug.erp.pessoa.contato.api;

import com.traxup.tplug.erp.pessoa.contato.PessoaContatoApplicationService.SituacaoContatos;

public record PessoaContatosSituacaoResponse(
        long quantidade, int minimoObrigatorio, boolean possuiPrincipal, boolean cadastroCompleto) {

    public static PessoaContatosSituacaoResponse from(SituacaoContatos situacao) {
        return new PessoaContatosSituacaoResponse(situacao.quantidade(), situacao.minimoObrigatorio(),
                situacao.possuiPrincipal(), situacao.cadastroCompleto());
    }
}
