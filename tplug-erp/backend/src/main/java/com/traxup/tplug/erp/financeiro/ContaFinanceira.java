package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contas_financeiras")
public class ContaFinanceira {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false, length = 20) private String tipo;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal saldo;
    @Column(nullable = false) private boolean ativo;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;
    @Version @Column(nullable = false) private long versao;

    protected ContaFinanceira() {}

    public ContaFinanceira(UUID tenantId, UUID filialId, String nome, String tipo, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.nome = nome;
        this.tipo = tipo;
        this.saldo = BigDecimal.ZERO;
        this.ativo = true;
        this.usuarioId = usuarioId;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (saldo == null) saldo = BigDecimal.ZERO;
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public void movimentar(String tipoMovimento, BigDecimal valor) {
        if (!ativo) throw new IllegalArgumentException("Conta financeira inativa nao pode ser movimentada");
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Valor do movimento deve ser maior que zero");
        if ("ENTRADA".equals(tipoMovimento)) {
            saldo = saldo.add(valor);
        } else if ("SAIDA".equals(tipoMovimento)) {
            if (valor.compareTo(saldo) > 0) throw new IllegalArgumentException("Saldo insuficiente para a saida financeira");
            saldo = saldo.subtract(valor);
        } else {
            throw new IllegalArgumentException("Tipo de movimento invalido");
        }
    }

    public void desativar() {
        if (!ativo) throw new IllegalArgumentException("Conta financeira ja esta inativa");
        ativo = false;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public boolean isAtivo() { return ativo; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public long getVersao() { return versao; }
}
