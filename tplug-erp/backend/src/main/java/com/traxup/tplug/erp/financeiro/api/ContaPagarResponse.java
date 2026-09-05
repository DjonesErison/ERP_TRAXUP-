package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaPagar;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContaPagarResponse(
        UUID id,
        UUID filialId,
        UUID fornecedorId,
        String numeroDocumento,
        String descricao,
        BigDecimal valorOriginal,
        BigDecimal valorPago,
        LocalDate vencimento,
        String status,
        UUID usuarioId,
        Instant pagoEm,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ContaPagarResponse from(ContaPagar conta) {
        return new ContaPagarResponse(conta.getId(), conta.getFilialId(), conta.getFornecedorId(),
                conta.getNumeroDocumento(), conta.getDescricao(), conta.getValorOriginal(), conta.getValorPago(),
                conta.getVencimento(), conta.getStatus(), conta.getUsuarioId(), conta.getPagoEm(),
                conta.getCriadoEm(), conta.getAtualizadoEm());
    }
}
