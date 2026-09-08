package com.traxup.tplug.erp.produto.combo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produto_combo_componentes")
public class ProdutoComboComponente {
    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "combo_produto_id", nullable = false)
    private UUID comboProdutoId;

    @Column(name = "componente_produto_id", nullable = false)
    private UUID componenteProdutoId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ProdutoComboComponente() {}

    public ProdutoComboComponente(UUID tenantId, UUID comboProdutoId, UUID componenteProdutoId, BigDecimal quantidade) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.comboProdutoId = comboProdutoId;
        this.componenteProdutoId = componenteProdutoId;
        this.quantidade = quantidade;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getComboProdutoId() { return comboProdutoId; }
    public UUID getComponenteProdutoId() { return componenteProdutoId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
