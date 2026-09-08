package com.traxup.tplug.erp.financeiro;

import java.math.BigDecimal;

public interface ConciliacaoResumoProjection {
    Long getTotalLancamentos();
    BigDecimal getValorTotal();
    Long getPendentes();
    BigDecimal getValorPendente();
    Long getConciliados();
    BigDecimal getValorConciliado();
    Long getTaxas();
    BigDecimal getValorTaxas();
    Long getAntecipacoes();
    BigDecimal getValorAntecipacoes();
    Long getEstornos();
    BigDecimal getValorEstornos();
    Long getChargebacks();
    BigDecimal getValorChargebacks();
}
