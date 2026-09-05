package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contas_financeiras_movimentos")
public class ContaFinanceiraMovimento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "conta_financeira_id", nullable = false) private UUID contaFinanceiraId;
    @Column(nullable = false, length = 20) private String tipo;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal valor;
    @Column(nullable = false, length = 200) private String descricao;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "ocorrido_em", nullable = false) private Instant ocorridoEm;

    protected ContaFinanceiraMovimento() {}

    public ContaFinanceiraMovimento(UUID tenantId, UUID filialId, UUID contaFinanceiraId,
                                    String tipo, BigDecimal valor, String descricao, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.contaFinanceiraId = contaFinanceiraId;
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.usuarioId = usuarioId;
        this.ocorridoEm = Instant.now();
    }

    @PrePersist void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (ocorridoEm == null) ocorridoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getContaFinanceiraId() { return contaFinanceiraId; }
    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getOcorridoEm() { return ocorridoEm; }
}
