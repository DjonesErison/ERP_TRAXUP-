package com.traxup.tplug.erp.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pedido_compra_itens")
public class PedidoCompraItem {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "pedido_compra_id", nullable = false)
    private UUID pedidoCompraId;

    @Column(name = "tipo_item", nullable = false, length = 10)
    private String tipoItem;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precoUnitario;

    @Column(name = "total_item", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalItem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PedidoCompraItem() {}

    public PedidoCompraItem(UUID tenantId, UUID pedidoCompraId, String tipoItem, UUID itemId,
                            BigDecimal quantidade, BigDecimal precoUnitario) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.pedidoCompraId = pedidoCompraId;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.totalItem = quantidade.multiply(precoUnitario);
    }

    @PrePersist
    void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        totalItem = quantidade.multiply(precoUnitario);
        atualizadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getPedidoCompraId() { return pedidoCompraId; }
    public String getTipoItem() { return tipoItem; }
    public UUID getItemId() { return itemId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getTotalItem() { return totalItem; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
