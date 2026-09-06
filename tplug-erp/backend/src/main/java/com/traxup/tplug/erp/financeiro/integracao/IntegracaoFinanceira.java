package com.traxup.tplug.erp.financeiro.integracao;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "integracoes_financeiras")
public class IntegracaoFinanceira {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "conta_financeira_id", nullable = false) private UUID contaFinanceiraId;
    @Column(nullable = false, length = 40) private String provedor;
    @Column(name = "identificador_externo", length = 120) private String identificadorExterno;
    @Column(nullable = false) private boolean ativo;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected IntegracaoFinanceira() {}

    public IntegracaoFinanceira(UUID tenantId, UUID filialId, UUID contaFinanceiraId, String provedor,
                                String identificadorExterno, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.contaFinanceiraId = contaFinanceiraId;
        this.provedor = normalizarProvedor(provedor);
        this.identificadorExterno = normalizarOpcional(identificadorExterno);
        this.ativo = true;
        this.usuarioId = usuarioId;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public void desativar() {
        if (!ativo) throw new IllegalArgumentException("Integracao financeira ja esta inativa");
        ativo = false;
    }

    private String normalizarProvedor(String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Provedor e obrigatorio");
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getContaFinanceiraId() { return contaFinanceiraId; }
    public String getProvedor() { return provedor; }
    public String getIdentificadorExterno() { return identificadorExterno; }
    public boolean isAtivo() { return ativo; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
