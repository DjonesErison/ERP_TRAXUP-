package com.traxup.tplug.erp.produto.combo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produto_combo_grupos")
public class ProdutoComboGrupo {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "combo_produto_id", nullable = false) private UUID comboProdutoId;
    @Column(nullable = false, length = 120) private String nome;
    @Column(name = "minimo_escolhas", nullable = false) private int minimoEscolhas;
    @Column(name = "maximo_escolhas", nullable = false) private int maximoEscolhas;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected ProdutoComboGrupo() {}

    public ProdutoComboGrupo(UUID tenantId, UUID comboProdutoId, String nome, int minimoEscolhas, int maximoEscolhas) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.comboProdutoId = comboProdutoId;
        this.nome = nome;
        this.minimoEscolhas = minimoEscolhas;
        this.maximoEscolhas = maximoEscolhas;
    }

    @PrePersist void aoCriar() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getComboProdutoId() { return comboProdutoId; }
    public String getNome() { return nome; }
    public int getMinimoEscolhas() { return minimoEscolhas; }
    public int getMaximoEscolhas() { return maximoEscolhas; }
}
