package com.traxup.tplug.erp.produto.combo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produto_combo_vigencias")
public class ProdutoComboVigencia {
    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "combo_produto_id", nullable = false)
    private UUID comboProdutoId;

    @Column(name = "vigencia_inicio")
    private Instant vigenciaInicio;

    @Column(name = "vigencia_fim")
    private Instant vigenciaFim;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ProdutoComboVigencia() {}

    public ProdutoComboVigencia(UUID tenantId, UUID comboProdutoId, Instant vigenciaInicio, Instant vigenciaFim) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.comboProdutoId = comboProdutoId;
        definir(vigenciaInicio, vigenciaFim);
    }

    public void definir(Instant vigenciaInicio, Instant vigenciaFim) {
        if (vigenciaInicio != null && vigenciaFim != null && vigenciaInicio.isAfter(vigenciaFim)) {
            throw new IllegalArgumentException("Inicio da vigencia nao pode ser posterior ao fim");
        }
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
    }

    public boolean vigenteEm(Instant instante) {
        if (instante == null) throw new IllegalArgumentException("Instante de referencia e obrigatorio");
        return (vigenciaInicio == null || !instante.isBefore(vigenciaInicio))
                && (vigenciaFim == null || !instante.isAfter(vigenciaFim));
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getComboProdutoId() { return comboProdutoId; }
    public Instant getVigenciaInicio() { return vigenciaInicio; }
    public Instant getVigenciaFim() { return vigenciaFim; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
