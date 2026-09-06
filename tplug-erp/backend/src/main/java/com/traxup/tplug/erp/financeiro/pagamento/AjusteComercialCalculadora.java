package com.traxup.tplug.erp.financeiro.pagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class AjusteComercialCalculadora {
    private static final BigDecimal CEM = new BigDecimal("100");
    private static final int ESCALA = 4;

    private AjusteComercialCalculadora() {}

    public static Resultado calcular(BigDecimal totalLiquido, CondicaoPagamento condicao) {
        if (totalLiquido == null || totalLiquido.signum() <= 0) {
            throw new IllegalArgumentException("Total liquido da venda deve ser positivo");
        }
        BigDecimal desconto = calcularAjuste(totalLiquido, condicao.getDescontoTipo(), condicao.getDescontoValor());
        if (desconto.compareTo(totalLiquido) > 0) {
            throw new IllegalArgumentException("Desconto nao pode superar o total liquido da venda");
        }
        BigDecimal aposDesconto = totalLiquido.subtract(desconto).setScale(ESCALA, RoundingMode.HALF_UP);
        BigDecimal juros = calcularAjuste(aposDesconto, condicao.getJurosTipo(), condicao.getJurosValor());
        BigDecimal totalFinanceiro = aposDesconto.add(juros).setScale(ESCALA, RoundingMode.HALF_UP);
        if (totalFinanceiro.signum() <= 0) {
            throw new IllegalArgumentException("Total financeiro da venda deve ser positivo");
        }
        BigDecimal entrada = calcularAjuste(totalFinanceiro, condicao.getEntradaTipo(), condicao.getEntradaValor());
        if (entrada.compareTo(totalFinanceiro) > 0) {
            throw new IllegalArgumentException("Entrada nao pode superar o total financeiro da venda");
        }
        BigDecimal saldoParcelar = totalFinanceiro.subtract(entrada).setScale(ESCALA, RoundingMode.HALF_UP);
        return new Resultado(totalLiquido.setScale(ESCALA, RoundingMode.HALF_UP), desconto, juros, entrada, totalFinanceiro, saldoParcelar);
    }

    private static BigDecimal calcularAjuste(BigDecimal base, AjusteComercialTipo tipo, BigDecimal valor) {
        if (tipo == null && valor == null) return BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
        if (tipo == null || valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Tipo e valor positivo do ajuste comercial devem ser informados em conjunto");
        }
        BigDecimal calculado = tipo == AjusteComercialTipo.PERCENTUAL
                ? base.multiply(valor).divide(CEM, ESCALA, RoundingMode.HALF_UP)
                : valor.setScale(ESCALA, RoundingMode.HALF_UP);
        return calculado.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public record Resultado(BigDecimal totalLiquido, BigDecimal desconto, BigDecimal juros, BigDecimal entrada,
                            BigDecimal totalFinanceiro, BigDecimal saldoParcelar) {}
}
