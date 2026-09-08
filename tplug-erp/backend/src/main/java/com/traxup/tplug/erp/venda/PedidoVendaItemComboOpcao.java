package com.traxup.tplug.erp.venda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

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
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;

    protected PedidoVendaItemComboOpcao() {}

    public PedidoVendaItemComboOpcao(UUID tenantId, UUID pedidoVendaItemId, UUID grupoId, UUID opcaoId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.pedidoVendaItemId = pedidoVendaItemId;
        this.grupoId = grupoId;
        this.opcaoId = opcaoId;
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
    public Instant getCriadoEm() { return criadoEm; }
}
