package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaFinanceira;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContaFinanceiraResponse(
        UUID id,
        UUID filialId,
        String nome,
        String tipo,
        BigDecimal saldo,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm,
        long versao
) {
    public static ContaFinanceiraResponse from(ContaFinanceira conta) {
        return new ContaFinanceiraResponse(conta.getId(), conta.getFilialId(), conta.getNome(), conta.getTipo(),
                conta.getSaldo(), conta.isAtivo(), conta.getCriadoEm(), conta.getAtualizadoEm(), conta.getVersao());
    }
}
