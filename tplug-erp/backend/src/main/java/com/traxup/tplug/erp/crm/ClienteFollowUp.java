package com.traxup.tplug.erp.crm;

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
@Table(name = "crm_cliente_followups")
public class ClienteFollowUp {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "cliente_id", nullable = false) private UUID clienteId;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 160) private String assunto;
    @Column(length = 500) private String observacao;
    @Column(name = "agendado_para", nullable = false) private Instant agendadoPara;
    @Column(name = "criado_por_id") private UUID criadoPorId;
    @Column(name = "concluido_por_id") private UUID concluidoPorId;
    @Column(name = "concluido_em") private Instant concluidoEm;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected ClienteFollowUp() {}

    public ClienteFollowUp(UUID tenantId, UUID filialId, UUID clienteId, String assunto,
                           String observacao, Instant agendadoPara, UUID criadoPorId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.clienteId = clienteId;
        this.status = "PENDENTE";
        this.assunto = assunto;
        this.observacao = observacao;
        this.agendadoPara = agendadoPara;
        this.criadoPorId = criadoPorId;
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
        if (!"PENDENTE".equals(status)) {
            throw new RegraNegocioException("Somente follow-up PENDENTE pode ser concluido");
        }
        status = "CONCLUIDO";
        concluidoPorId = usuarioId;
        concluidoEm = Instant.now();
    }

    public void cancelar(UUID usuarioId) {
        if (!"PENDENTE".equals(status)) {
            throw new RegraNegocioException("Somente follow-up PENDENTE pode ser cancelado");
        }
        status = "CANCELADO";
        concluidoPorId = usuarioId;
        concluidoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getClienteId() { return clienteId; }
    public String getStatus() { return status; }
    public String getAssunto() { return assunto; }
    public String getObservacao() { return observacao; }
    public Instant getAgendadoPara() { return agendadoPara; }
    public UUID getCriadoPorId() { return criadoPorId; }
    public UUID getConcluidoPorId() { return concluidoPorId; }
    public Instant getConcluidoEm() { return concluidoEm; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
