package com.traxup.tplug.erp.crm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "crm_cliente_interacoes")
public class ClienteInteracao {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;
    @Column(name = "followup_id")
    private UUID followUpId;
    @Column(nullable = false, length = 20)
    private String canal;
    @Column(nullable = false, length = 30)
    private String resultado;
    @Column(nullable = false, length = 160)
    private String assunto;
    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;
    @Column(name = "usuario_id")
    private UUID usuarioId;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected ClienteInteracao() {}

    public ClienteInteracao(UUID tenantId, UUID filialId, UUID clienteId, UUID followUpId,
                            String canal, String resultado, String assunto, Instant ocorridoEm, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.clienteId = clienteId;
        this.followUpId = followUpId;
        this.canal = canal;
        this.resultado = resultado;
        this.assunto = assunto;
        this.ocorridoEm = ocorridoEm;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (criadoEm == null) criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getClienteId() { return clienteId; }
    public UUID getFollowUpId() { return followUpId; }
    public String getCanal() { return canal; }
    public String getResultado() { return resultado; }
    public String getAssunto() { return assunto; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
}
