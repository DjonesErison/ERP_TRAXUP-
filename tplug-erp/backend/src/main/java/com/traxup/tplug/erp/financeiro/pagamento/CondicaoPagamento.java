package com.traxup.tplug.erp.financeiro.pagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "condicoes_pagamento")
public class CondicaoPagamento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(nullable = false, length = 40) private String codigo;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false) private boolean ativo = true;
    @Enumerated(EnumType.STRING) @Column(name = "juros_tipo", length = 20) private AjusteComercialTipo jurosTipo;
    @Column(name = "juros_valor", precision = 15, scale = 4) private BigDecimal jurosValor;
    @Enumerated(EnumType.STRING) @Column(name = "desconto_tipo", length = 20) private AjusteComercialTipo descontoTipo;
    @Column(name = "desconto_valor", precision = 15, scale = 4) private BigDecimal descontoValor;
    @Enumerated(EnumType.STRING) @Column(name = "entrada_tipo", length = 20) private AjusteComercialTipo entradaTipo;
    @Column(name = "entrada_valor", precision = 15, scale = 4) private BigDecimal entradaValor;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected CondicaoPagamento() {}

    public CondicaoPagamento(UUID tenantId, String codigo, String nome) {
        this(tenantId, codigo, nome, null, null, null, null, null, null);
    }

    public CondicaoPagamento(UUID tenantId, String codigo, String nome,
                             AjusteComercialTipo jurosTipo, BigDecimal jurosValor,
                             AjusteComercialTipo descontoTipo, BigDecimal descontoValor,
                             AjusteComercialTipo entradaTipo, BigDecimal entradaValor) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.codigo = codigo;
        this.nome = nome;
        this.jurosTipo = jurosTipo;
        this.jurosValor = jurosValor;
        this.descontoTipo = descontoTipo;
        this.descontoValor = descontoValor;
        this.entradaTipo = entradaTipo;
        this.entradaValor = entradaValor;
    }

    @PrePersist void prePersist() { Instant agora = Instant.now(); criadoEm = agora; atualizadoEm = agora; }
    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }
    public void desativar() { this.ativo = false; }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public AjusteComercialTipo getJurosTipo() { return jurosTipo; }
    public BigDecimal getJurosValor() { return jurosValor; }
    public AjusteComercialTipo getDescontoTipo() { return descontoTipo; }
    public BigDecimal getDescontoValor() { return descontoValor; }
    public AjusteComercialTipo getEntradaTipo() { return entradaTipo; }
    public BigDecimal getEntradaValor() { return entradaValor; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
