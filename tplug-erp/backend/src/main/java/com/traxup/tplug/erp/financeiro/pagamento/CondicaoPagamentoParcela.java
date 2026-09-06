package com.traxup.tplug.erp.financeiro.pagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "condicoes_pagamento_parcelas")
public class CondicaoPagamentoParcela {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "condicao_pagamento_id", nullable = false) private UUID condicaoPagamentoId;
    @Column(nullable = false) private int numero;
    @Column(nullable = false) private int dias;
    @Column(nullable = false, precision = 7, scale = 4) private BigDecimal percentual;

    protected CondicaoPagamentoParcela() {}

    public CondicaoPagamentoParcela(UUID tenantId, UUID condicaoPagamentoId, int numero, int dias, BigDecimal percentual) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.condicaoPagamentoId = condicaoPagamentoId;
        this.numero = numero;
        this.dias = dias;
        this.percentual = percentual;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getCondicaoPagamentoId() { return condicaoPagamentoId; }
    public int getNumero() { return numero; }
    public int getDias() { return dias; }
    public BigDecimal getPercentual() { return percentual; }
}
