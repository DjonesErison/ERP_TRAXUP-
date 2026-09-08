package com.traxup.tplug.erp.venda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pedido_venda_item_combo_opcoes")
public class PedidoVendaItemComboOpcao {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "pedido_venda_item_id", nullable = false) private UUID pedidoVendaItemId;
    @Column(name = "grupo_id", nullable = false) private UUID grupoId;
    @Column(name = "opcao_id", nullable = false) private UUID opcaoId;
    @Column(name = "produto_id", nullable = false) private UUID produtoId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantidade;
    @Column(name = "valor_adicional", nullable = false, precision = 19, scale = 4) private BigDecimal valorAdicional;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;

    protected PedidoVendaItemComboOpcao() {}

    public PedidoVendaItemComboOpcao(UUID tenantId, UUID pedidoVendaItemId, UUID grupoId, UUID opcaoId,
                                     UUID produtoId, BigDecimal quantidade, BigDecimal valorAdicional) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.pedidoVendaItemId = pedidoVendaItemId;
        this.grupoId = grupoId;
        this.opcaoId = opcaoId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorAdicional = valorAdicional;
    }

    @PrePersist void aoCriar() {
        if (id == null) id = UUID.randomUUID();
        criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getPedidoVendaItemId() { return pedidoVendaItemId; }
    public UUID getGrupoId() { return grupoId; }
    public UUID getOpcaoId() { return opcaoId; }
    public UUID getProdutoId() { return produtoId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getValorAdicional() { return valorAdicional; }
    public Instant getCriadoEm() { return criadoEm; }
}
