package com.traxup.tplug.erp.fiscal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fiscal_solicitacoes")
public class FiscalSolicitacao {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(name = "pedido_venda_id", nullable = false)
    private UUID pedidoVendaId;
    @Column(nullable = false, length = 10)
    private String modelo;
    @Column(nullable = false, length = 20)
    private String ambiente;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected FiscalSolicitacao() {}

    public FiscalSolicitacao(UUID tenantId, UUID filialId, UUID pedidoVendaId, String modelo, String ambiente) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.pedidoVendaId = pedidoVendaId;
        this.modelo = modelo;
        this.ambiente = ambiente;
        this.status = "PENDENTE";
    }

    @PrePersist
    void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() { atualizadoEm = Instant.now(); }

    public boolean iniciarProcessamento() {
        if ("PROCESSANDO".equals(status)) return false;
        if (!"PENDENTE".equals(status)) {
            throw new IllegalArgumentException("Somente solicitacao PENDENTE pode iniciar processamento");
        }
        status = "PROCESSANDO";
        return true;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getPedidoVendaId() { return pedidoVendaId; }
    public String getModelo() { return modelo; }
    public String getAmbiente() { return ambiente; }
    public String getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
