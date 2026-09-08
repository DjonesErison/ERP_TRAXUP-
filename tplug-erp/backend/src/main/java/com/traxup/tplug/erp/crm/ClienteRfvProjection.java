package com.traxup.tplug.erp.crm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface ClienteRfvProjection {
    UUID getClienteId();
    String getNomeRazaoSocial();
    String getNomeFantasia();
    String getEmail();
    String getTelefone();
    Instant getUltimaCompraEm();
    long getQuantidadeCompras();
    BigDecimal getValorTotalCompras();
}
