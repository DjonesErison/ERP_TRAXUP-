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
@Table(name = "produto_combo_grupo_opcoes")
public class ProdutoComboGrupoOpcao {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "grupo_id", nullable = false) private UUID grupoId;
    @Column(name = "produto_id", nullable = false) private UUID produtoId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantidade;
    @Column(name = "valor_adicional", nullable = false, precision = 19, scale = 4) private BigDecimal valorAdicional;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected ProdutoComboGrupoOpcao() {}

    public ProdutoComboGrupoOpcao(UUID tenantId, UUID grupoId, UUID produtoId, BigDecimal quantidade, BigDecimal valorAdicional) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.grupoId = grupoId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorAdicional = valorAdicional;
    }

    @PrePersist void aoCriar() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getGrupoId() { return grupoId; }
    public UUID getProdutoId() { return produtoId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getValorAdicional() { return valorAdicional; }
}
