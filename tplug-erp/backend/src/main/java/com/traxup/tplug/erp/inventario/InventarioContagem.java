package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
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
@Table(name = "inventario_contagens")
public class InventarioContagem {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "inventario_id", nullable = false)
    private UUID inventarioId;
    @Column(name = "tipo_item", nullable = false, length = 10)
    private String tipoItem;
    @Column(name = "item_id", nullable = false)
    private UUID itemId;
    @Column(name = "quantidade_sistema", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidadeSistema;
    @Column(name = "quantidade_contada", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidadeContada;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal divergencia;
    @Column(name = "contado_por_id")
    private UUID contadoPorId;
    @Column(name = "contado_em", nullable = false)
    private Instant contadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected InventarioContagem() {}

    public InventarioContagem(UUID tenantId, UUID inventarioId, String tipoItem, UUID itemId,
                              BigDecimal quantidadeSistema, BigDecimal quantidadeContada, UUID contadoPorId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.inventarioId = inventarioId;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        atualizar(quantidadeSistema, quantidadeContada, contadoPorId);
    }

    public void atualizar(BigDecimal quantidadeSistema, BigDecimal quantidadeContada, UUID usuarioId) {
        if (quantidadeSistema == null) throw new RegraNegocioException("Quantidade do sistema e obrigatoria");
        if (quantidadeContada == null || quantidadeContada.signum() < 0) {
            throw new RegraNegocioException("Quantidade contada deve ser maior ou igual a zero");
        }
        this.quantidadeSistema = quantidadeSistema;
        this.quantidadeContada = quantidadeContada;
        this.divergencia = quantidadeContada.subtract(quantidadeSistema);
        this.contadoPorId = usuarioId;
        this.contadoEm = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (contadoEm == null) contadoEm = Instant.now();
        atualizadoEm = Instant.now();
    }

    @PreUpdate
    void preUpdate() { atualizadoEm = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getInventarioId() { return inventarioId; }
    public String getTipoItem() { return tipoItem; }
    public UUID getItemId() { return itemId; }
    public BigDecimal getQuantidadeSistema() { return quantidadeSistema; }
    public BigDecimal getQuantidadeContada() { return quantidadeContada; }
    public BigDecimal getDivergencia() { return divergencia; }
    public UUID getContadoPorId() { return contadoPorId; }
    public Instant getContadoEm() { return contadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
