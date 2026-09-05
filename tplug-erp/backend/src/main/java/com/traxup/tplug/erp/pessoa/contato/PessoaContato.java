package com.traxup.tplug.erp.pessoa.contato;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pessoa_contatos")
public class PessoaContato {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "pessoa_id", nullable = false)
    private UUID pessoaId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 120)
    private String cargo;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(nullable = false)
    private boolean principal;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PessoaContato() {}

    public PessoaContato(UUID tenantId, UUID pessoaId, String nome, String cargo, String email, String telefone, boolean principal) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.pessoaId = pessoaId;
        this.nome = nome;
        this.cargo = cargo;
        this.email = email;
        this.telefone = telefone;
        this.principal = principal;
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

    public void desmarcarPrincipal() {
        this.principal = false;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getPessoaId() { return pessoaId; }
    public String getNome() { return nome; }
    public String getCargo() { return cargo; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public boolean isPrincipal() { return principal; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
