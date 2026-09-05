package com.traxup.tplug.erp.venda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pedidos_venda")
public class PedidoVenda {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "cliente_id") private UUID clienteId;
    @Column(nullable = false, length = 40) private String numero;
    @Column(nullable = false, length = 20) private String status;
    @Column(length = 500) private String observacao;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected PedidoVenda() {}

    public PedidoVenda(UUID tenantId, UUID filialId, UUID clienteId, String numero, String observacao, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.clienteId = clienteId;
        this.numero = numero;
        this.status = "RASCUNHO";
        this.observacao = observacao;
        this.usuarioId = usuarioId;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public void abrir() {
        if (!"RASCUNHO".equals(status)) throw new IllegalArgumentException("Somente pedido de venda em RASCUNHO pode ser aberto");
        status = "ABERTO";
    }

    public void cancelar() {
        if ("CANCELADO".equals(status) || "FATURADO".equals(status)) throw new IllegalArgumentException("Pedido de venda nao pode ser cancelado no estado atual");
        status = "CANCELADO";
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getClienteId() { return clienteId; }
    public String getNumero() { return numero; }
    public String getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
