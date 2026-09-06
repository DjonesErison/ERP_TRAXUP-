package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaReceber;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContaReceberResponse(
        UUID id,
        UUID filialId,
        UUID clienteId,
        String numeroDocumento,
        String descricao,
        BigDecimal valorOriginal,
        BigDecimal valorRecebido,
        BigDecimal saldoAberto,
        LocalDate vencimento,
        String status,
        String origemTipo,
        UUID origemId,
        String origemReferencia,
        Instant recebidoEm,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ContaReceberResponse from(ContaReceber conta) {
        return new ContaReceberResponse(
                conta.getId(), conta.getFilialId(), conta.getClienteId(), conta.getNumeroDocumento(),
                conta.getDescricao(), conta.getValorOriginal(), conta.getValorRecebido(), conta.getSaldoAberto(),
                conta.getVencimento(), conta.getStatus(), conta.getOrigemTipo(), conta.getOrigemId(),
                conta.getOrigemReferencia(), conta.getRecebidoEm(), conta.getCriadoEm(), conta.getAtualizadoEm());
    }
}
