package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaReceberMovimento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContaReceberMovimentoResponse(
        UUID id,
        UUID contaReceberId,
        String tipo,
        BigDecimal valor,
        LocalDate dataMovimento,
        String observacao,
        UUID usuarioId,
        Instant criadoEm
) {
    public static ContaReceberMovimentoResponse from(ContaReceberMovimento movimento) {
        return new ContaReceberMovimentoResponse(
                movimento.getId(), movimento.getContaReceberId(), movimento.getTipo(), movimento.getValor(),
                movimento.getDataMovimento(), movimento.getObservacao(), movimento.getUsuarioId(),
                movimento.getCriadoEm());
    }
}
