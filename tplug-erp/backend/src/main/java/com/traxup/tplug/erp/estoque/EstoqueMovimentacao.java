package com.traxup.tplug.erp.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "estoque_movimentacoes")
public class EstoqueMovimentacao {

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

    @Column(name = "tipo_movimento", nullable = false, length = 10)
    private String tipoMovimento;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "saldo_anterior", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoPosterior;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected EstoqueMovimentacao() {}

    public EstoqueMovimentacao(UUID tenantId, UUID filialId, String tipoItem, UUID itemId,
                               String tipoMovimento, BigDecimal quantidade, BigDecimal saldoAnterior,
                               BigDecimal saldoPosterior, String motivo, UUID usuarioId) {
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.tipoMovimento = tipoMovimento;
        this.quantidade = quantidade;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.motivo = motivo;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void aoCriar() { criadoEm = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public String getTipoItem() { return tipoItem; }
    public UUID getItemId() { return itemId; }
    public String getTipoMovimento() { return tipoMovimento; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public String getMotivo() { return motivo; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
}
