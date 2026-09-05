package com.traxup.tplug.erp.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "estoque_saldos")
public class EstoqueSaldo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "tipo_item", nullable = false, length = 10)
    private String tipoItem;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade = BigDecimal.ZERO;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected EstoqueSaldo() {
    }

    public EstoqueSaldo(UUID tenantId, UUID filialId, String tipoItem, UUID itemId) {
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.quantidade = BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    void atualizarTimestamp() {
        atualizadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public String getTipoItem() { return tipoItem; }
    public UUID getItemId() { return itemId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void definirQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }
}
