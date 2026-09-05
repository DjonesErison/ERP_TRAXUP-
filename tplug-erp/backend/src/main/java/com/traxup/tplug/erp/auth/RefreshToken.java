package com.traxup.tplug.erp.auth;

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
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "revogado_em")
    private Instant revogadoEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected RefreshToken() {
    }

    public RefreshToken(UUID tenantId, UUID usuarioId, String tokenHash, Instant expiraEm) {
        this.tenantId = tenantId;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiraEm = expiraEm;
    }

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getRevogadoEm() {
        return revogadoEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public boolean estaRevogado() {
        return revogadoEm != null;
    }

    public boolean estaExpirado(Instant agora) {
        return !expiraEm.isAfter(agora);
    }

    public void revogar() {
        if (revogadoEm == null) {
            revogadoEm = Instant.now();
        }
    }
}
