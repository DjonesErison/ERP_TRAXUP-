package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventario_sessoes")
public class InventarioSessao {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(length = 160)
    private String descricao;
    @Column(name = "contagem_cega", nullable = false)
    private boolean contagemCega;
    @Column(name = "criado_por_id")
    private UUID criadoPorId;
    @Column(name = "concluido_por_id")
    private UUID concluidoPorId;
    @Column(name = "ajustado_por_id")
    private UUID ajustadoPorId;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;
    @Column(name = "concluido_em")
    private Instant concluidoEm;
    @Column(name = "ajustado_em")
    private Instant ajustadoEm;

    protected InventarioSessao() {}

    public InventarioSessao(UUID tenantId, UUID filialId, String descricao, UUID criadoPorId) {
        this(tenantId, filialId, descricao, criadoPorId, false);
    }

    public InventarioSessao(UUID tenantId, UUID filialId, String descricao, UUID criadoPorId, boolean contagemCega) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.status = "ABERTO";
        this.descricao = descricao;
        this.criadoPorId = criadoPorId;
        this.contagemCega = contagemCega;
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

    public void concluir(UUID usuarioId) {
        if (!"ABERTO".equals(status)) throw new RegraNegocioException("Somente inventario ABERTO pode ser concluido");
        status = "CONCLUIDO";
        concluidoPorId = usuarioId;
        concluidoEm = Instant.now();
    }

    public void cancelar(UUID usuarioId) {
        if (!"ABERTO".equals(status)) throw new RegraNegocioException("Somente inventario ABERTO pode ser cancelado");
        status = "CANCELADO";
        concluidoPorId = usuarioId;
        concluidoEm = Instant.now();
    }

    public void marcarAjustado(UUID usuarioId) {
        if (!"CONCLUIDO".equals(status)) throw new RegraNegocioException("Somente inventario CONCLUIDO pode ajustar estoque");
        if (ajustadoEm != null) throw new RegraNegocioException("Estoque deste inventario ja foi ajustado");
        ajustadoPorId = usuarioId;
        ajustadoEm = Instant.now();
    }

    public boolean deveOcultarSaldoDuranteContagem() {
        return contagemCega && "ABERTO".equals(status);
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public String getStatus() { return status; }
    public String getDescricao() { return descricao; }
    public boolean isContagemCega() { return contagemCega; }
    public UUID getCriadoPorId() { return criadoPorId; }
    public UUID getConcluidoPorId() { return concluidoPorId; }
    public UUID getAjustadoPorId() { return ajustadoPorId; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public Instant getConcluidoEm() { return concluidoEm; }
    public Instant getAjustadoEm() { return ajustadoEm; }
}
