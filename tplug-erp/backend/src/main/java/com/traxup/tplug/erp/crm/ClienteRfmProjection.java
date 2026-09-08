package com.traxup.tplug.erp.crm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface ClienteRfmProjection {
    UUID getClienteId();
    String getNomeRazaoSocial();
    String getNomeFantasia();
    Instant getUltimaCompraEm();
    Long getQuantidadeCompras();
    BigDecimal getValorTotal();
}
