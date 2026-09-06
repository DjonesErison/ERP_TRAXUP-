package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.financeiro.ContaPagarPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContaPagarPagamentoResponse(
        UUID id,
        UUID filialId,
        UUID contaPagarId,
        BigDecimal valor,
        UUID usuarioId,
        Instant pagoEm
) {
    public static ContaPagarPagamentoResponse from(ContaPagarPagamento pagamento) {
        return new ContaPagarPagamentoResponse(
                pagamento.getId(), pagamento.getFilialId(), pagamento.getContaPagarId(),
                pagamento.getValor(), pagamento.getUsuarioId(), pagamento.getPagoEm());
    }
}
