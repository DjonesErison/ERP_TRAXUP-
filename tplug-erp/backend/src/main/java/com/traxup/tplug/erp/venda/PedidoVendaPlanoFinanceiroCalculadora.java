package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.financeiro.pagamento.AjusteComercialCalculadora;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PedidoVendaPlanoFinanceiroCalculadora {
    private static final int ESCALA = 4;
    private static final BigDecimal CEM = new BigDecimal("100");

    private PedidoVendaPlanoFinanceiroCalculadora() {}

    public static Plano calcular(BigDecimal totalLiquido, CondicaoPagamento condicao,
                                 List<CondicaoPagamentoParcela> parcelas, LocalDate dataBase) {
        if (totalLiquido == null || totalLiquido.signum() <= 0) {
            throw new IllegalArgumentException("Total liquido da venda deve ser positivo");
        }
        if (dataBase == null) throw new IllegalArgumentException("Data base do plano financeiro deve ser informada");

        if (condicao == null) {
            BigDecimal total = totalLiquido.setScale(ESCALA, RoundingMode.HALF_UP);
            BigDecimal zero = BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
            return new Plano(total, zero, zero, zero, total, total,
                    List.of(new Titulo("PARCELA", 1, dataBase, total)));
        }

        AjusteComercialCalculadora.Resultado ajustes = AjusteComercialCalculadora.calcular(totalLiquido, condicao);
        List<Titulo> titulos = new ArrayList<>();

        if (ajustes.entrada().signum() > 0) {
            titulos.add(new Titulo("ENTRADA", 0, dataBase, ajustes.entrada()));
        }

        if (ajustes.saldoParcelar().signum() > 0) {
            if (parcelas == null || parcelas.isEmpty()) {
                throw new IllegalArgumentException("Condicao de pagamento precisa possuir parcelas");
            }
            BigDecimal acumulado = BigDecimal.ZERO.setScale(ESCALA, RoundingMode.HALF_UP);
            for (int i = 0; i < parcelas.size(); i++) {
                CondicaoPagamentoParcela parcela = parcelas.get(i);
                BigDecimal valor = i == parcelas.size() - 1
                        ? ajustes.saldoParcelar().subtract(acumulado).setScale(ESCALA, RoundingMode.HALF_UP)
                        : ajustes.saldoParcelar().multiply(parcela.getPercentual())
                        .divide(CEM, ESCALA, RoundingMode.HALF_UP);
                acumulado = acumulado.add(valor);
                titulos.add(new Titulo("PARCELA", parcela.getNumero(), dataBase.plusDays(parcela.getDias()), valor));
            }
        }

        return new Plano(ajustes.totalLiquido(), ajustes.desconto(), ajustes.juros(), ajustes.entrada(),
                ajustes.totalFinanceiro(), ajustes.saldoParcelar(), List.copyOf(titulos));
    }

    public record Plano(BigDecimal totalLiquido, BigDecimal desconto, BigDecimal juros, BigDecimal entrada,
                        BigDecimal totalFinanceiro, BigDecimal saldoParcelar, List<Titulo> titulos) {}

    public record Titulo(String tipo, int numero, LocalDate vencimento, BigDecimal valor) {}
}
