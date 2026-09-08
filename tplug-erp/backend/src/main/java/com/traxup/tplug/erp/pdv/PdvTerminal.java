package com.traxup.tplug.erp.pdv;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pdv_terminais")
public class PdvTerminal {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(nullable = false, length = 64)
    private String codigo;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false)
    private Integer serie;
    @Column(nullable = false)
    private boolean ativo;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PdvTerminal() {}

    public PdvTerminal(UUID tenantId, UUID filialId, String codigo, String nome, Integer serie) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.codigo = codigo;
        this.nome = nome;
        this.serie = serie;
        this.ativo = true;
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

    public void ativar() { ativo = true; }
    public void desativar() { ativo = false; }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public Integer getSerie() { return serie; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
