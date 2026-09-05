package com.traxup.tplug.erp.permissao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "permissoes")
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chave", nullable = false, unique = true, length = 120)
    private String chave;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Permissao() {
    }

    public Permissao(String chave, String descricao) {
        this.chave = chave;
        this.descricao = descricao;
    }

    @PrePersist
    protected void aoCriar() {
        criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
