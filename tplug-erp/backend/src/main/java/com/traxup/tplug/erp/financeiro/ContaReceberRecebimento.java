package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contas_receber_recebimentos")
public class ContaReceberRecebimento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "conta_receber_id", nullable = false) private UUID contaReceberId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal valor;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "recebido_em", nullable = false) private Instant recebidoEm;

    protected ContaReceberRecebimento() {}

    public ContaReceberRecebimento(UUID tenantId, UUID filialId, UUID contaReceberId,
                                   BigDecimal valor, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.contaReceberId = contaReceberId;
        this.valor = valor;
        this.usuarioId = usuarioId;
        this.recebidoEm = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (recebidoEm == null) recebidoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getContaReceberId() { return contaReceberId; }
    public BigDecimal getValor() { return valor; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getRecebidoEm() { return recebidoEm; }
}
