package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contas_receber_movimentos")
public class ContaReceberMovimento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "conta_receber_id", nullable = false) private UUID contaReceberId;
    @Column(nullable = false, length = 20) private String tipo;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal valor;
    @Column(name = "data_movimento", nullable = false) private LocalDate dataMovimento;
    @Column(length = 500) private String observacao;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;

    protected ContaReceberMovimento() {}

    public ContaReceberMovimento(UUID tenantId, UUID contaReceberId, BigDecimal valor,
                                 LocalDate dataMovimento, String observacao, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.contaReceberId = contaReceberId;
        this.tipo = "RECEBIMENTO";
        this.valor = valor;
        this.dataMovimento = dataMovimento;
        this.observacao = observacao;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getContaReceberId() { return contaReceberId; }
    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataMovimento() { return dataMovimento; }
    public String getObservacao() { return observacao; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
}
