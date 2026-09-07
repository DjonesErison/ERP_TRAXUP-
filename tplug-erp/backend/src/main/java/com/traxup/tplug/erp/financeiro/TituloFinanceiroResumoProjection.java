package com.traxup.tplug.erp.financeiro;

import java.math.BigDecimal;

public interface TituloFinanceiroResumoProjection {
    Long getQuantidade();
    BigDecimal getValorOriginalTotal();
    BigDecimal getValorLiquidadoTotal();
    BigDecimal getSaldoAtivoTotal();
    Long getAbertos();
    Long getParciais();
    Long getLiquidados();
    Long getCancelados();
}
