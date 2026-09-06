package com.traxup.tplug.erp.financeiro.pagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "formas_pagamento")
public class FormaPagamento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(nullable = false, length = 40) private String codigo;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected FormaPagamento() {}

    public FormaPagamento(UUID tenantId, String codigo, String nome) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.codigo = codigo;
        this.nome = nome;
        this.ativo = true;
    }

    @PrePersist void prePersist() { Instant agora = Instant.now(); criadoEm = agora; atualizadoEm = agora; }
    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }
    public void desativar() { this.ativo = false; }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
