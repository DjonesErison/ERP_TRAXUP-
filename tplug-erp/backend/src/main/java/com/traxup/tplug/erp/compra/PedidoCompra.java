package com.traxup.tplug.erp.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pedidos_compra")
public class PedidoCompra {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "fornecedor_id", nullable = false) private UUID fornecedorId;
    @Column(nullable = false, length = 40) private String numero;
    @Column(nullable = false, length = 20) private String status;
    @Column(length = 500) private String observacao;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected PedidoCompra() {}

    public PedidoCompra(UUID tenantId, UUID filialId, UUID fornecedorId, String numero, String observacao) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.filialId = filialId;
        this.fornecedorId = fornecedorId; this.numero = numero; this.status = "RASCUNHO"; this.observacao = observacao;
    }

    @PrePersist void prePersist() { Instant agora = Instant.now(); if (id == null) id = UUID.randomUUID(); criadoEm = agora; atualizadoEm = agora; }
    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public void abrir() {
        if (!"RASCUNHO".equals(status)) throw new IllegalArgumentException("Somente pedido em RASCUNHO pode ser aberto");
        status = "ABERTO";
    }

    public void cancelar() {
        if ("CANCELADO".equals(status) || "RECEBIDO".equals(status)) throw new IllegalArgumentException("Pedido nao pode ser cancelado no estado atual");
        status = "CANCELADO";
    }

    public void marcarRecebido() {
        if (!"ABERTO".equals(status)) throw new IllegalArgumentException("Somente pedido ABERTO pode ser marcado como recebido");
        status = "RECEBIDO";
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getFornecedorId() { return fornecedorId; }
    public String getNumero() { return numero; }
    public String getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
