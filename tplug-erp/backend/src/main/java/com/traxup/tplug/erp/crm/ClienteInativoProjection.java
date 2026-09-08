package com.traxup.tplug.erp.crm;

import java.time.Instant;
import java.util.UUID;

public interface ClienteInativoProjection {
    UUID getClienteId();
    String getNomeRazaoSocial();
    String getNomeFantasia();
    String getEmail();
    String getTelefone();
    Instant getUltimaCompraEm();
    long getQuantidadeCompras();
}
