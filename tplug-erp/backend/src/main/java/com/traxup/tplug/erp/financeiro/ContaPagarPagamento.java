package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contas_pagar_pagamentos")
public class ContaPagarPagamento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "conta_pagar_id", nullable = false) private UUID contaPagarId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal valor;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "pago_em", nullable = false) private Instant pagoEm;

    protected ContaPagarPagamento() {}

    public ContaPagarPagamento(UUID tenantId, UUID filialId, UUID contaPagarId, BigDecimal valor, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.contaPagarId = contaPagarId;
        this.valor = valor;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (pagoEm == null) pagoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getContaPagarId() { return contaPagarId; }
    public BigDecimal getValor() { return valor; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getPagoEm() { return pagoEm; }
}
