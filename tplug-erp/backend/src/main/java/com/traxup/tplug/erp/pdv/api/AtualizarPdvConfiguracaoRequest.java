package com.traxup.tplug.erp.pdv.api;

import jakarta.validation.constraints.NotBlank;

public record AtualizarPdvConfiguracaoRequest(
        boolean exigirJustificativaCancelamento,
        boolean exigirAutorizacaoCancelamento,
        @NotBlank String tamanhoImpressao,
        boolean imprimirCaixa,
        boolean imprimirCozinha) {}
