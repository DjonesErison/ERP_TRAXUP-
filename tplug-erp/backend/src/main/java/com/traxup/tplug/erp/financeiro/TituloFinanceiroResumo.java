package com.traxup.tplug.erp.financeiro;

import java.math.BigDecimal;
import java.util.List;

public record TituloFinanceiroResumo(
        long quantidade,
        BigDecimal valorOriginalTotal,
        BigDecimal valorLiquidadoTotal,
        BigDecimal saldoAtivoTotal,
        long abertos,
        long parciais,
        long liquidados,
        long cancelados) {

    public static TituloFinanceiroResumo deContasReceber(List<ContaReceber> contas) {
        BigDecimal original = BigDecimal.ZERO;
        BigDecimal liquidado = BigDecimal.ZERO;
        BigDecimal saldoAtivo = BigDecimal.ZERO;
        long abertos = 0;
        long parciais = 0;
        long liquidados = 0;
        long cancelados = 0;

        for (ContaReceber conta : contas) {
            original = original.add(conta.getValorOriginal());
            liquidado = liquidado.add(conta.getValorRecebido());
            switch (conta.getStatus()) {
                case "ABERTO" -> {
                    abertos++;
                    saldoAtivo = saldoAtivo.add(conta.getSaldoAberto());
                }
                case "PARCIAL" -> {
                    parciais++;
                    saldoAtivo = saldoAtivo.add(conta.getSaldoAberto());
                }
                case "RECEBIDO" -> liquidados++;
                case "CANCELADO" -> cancelados++;
                default -> throw new IllegalStateException("Status inesperado de conta a receber: " + conta.getStatus());
            }
        }
        return new TituloFinanceiroResumo(contas.size(), original, liquidado, saldoAtivo,
                abertos, parciais, liquidados, cancelados);
    }

    public static TituloFinanceiroResumo deContasPagar(List<ContaPagar> contas) {
        BigDecimal original = BigDecimal.ZERO;
        BigDecimal liquidado = BigDecimal.ZERO;
        BigDecimal saldoAtivo = BigDecimal.ZERO;
        long abertos = 0;
        long parciais = 0;
        long liquidados = 0;
        long cancelados = 0;

        for (ContaPagar conta : contas) {
            original = original.add(conta.getValorOriginal());
            liquidado = liquidado.add(conta.getValorPago());
            switch (conta.getStatus()) {
                case "ABERTO" -> {
                    abertos++;
                    saldoAtivo = saldoAtivo.add(conta.getSaldoAberto());
                }
                case "PARCIAL" -> {
                    parciais++;
                    saldoAtivo = saldoAtivo.add(conta.getSaldoAberto());
                }
                case "PAGO" -> liquidados++;
                case "CANCELADO" -> cancelados++;
                default -> throw new IllegalStateException("Status inesperado de conta a pagar: " + conta.getStatus());
            }
        }
        return new TituloFinanceiroResumo(contas.size(), original, liquidado, saldoAtivo,
                abertos, parciais, liquidados, cancelados);
    }
}
