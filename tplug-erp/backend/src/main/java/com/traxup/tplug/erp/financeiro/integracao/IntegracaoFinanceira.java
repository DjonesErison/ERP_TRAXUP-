package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
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
    @Column(length = 500) private String checkpoint;
    @Column(name = "sincronizado_em") private Instant sincronizadoEm;
    @Column(nullable = false) private boolean ativo;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;
    @Version @Column(nullable = false) private long versao;

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
        if (!ativo) throw new RegraNegocioException("Integracao financeira ja esta inativa");
        ativo = false;
    }

    public void registrarSincronizacao(String checkpoint, Instant sincronizadoEm) {
        if (!ativo) throw new RecursoConflitanteException("Integracao financeira inativa nao pode ser sincronizada");
        if (sincronizadoEm == null) throw new RegraNegocioException("Data/hora da sincronizacao e obrigatoria");
        if (this.sincronizadoEm != null && !sincronizadoEm.isAfter(this.sincronizadoEm)) {
            throw new RecursoConflitanteException(
                    "Sincronizacao deve ser posterior ao ultimo checkpoint registrado");
        }
        this.checkpoint = normalizarCheckpoint(checkpoint);
        this.sincronizadoEm = sincronizadoEm;
    }

    private String normalizarProvedor(String valor) {
        if (valor == null || valor.isBlank()) throw new RegraNegocioException("Provedor e obrigatorio");
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarCheckpoint(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String normalizado = valor.trim();
        if (normalizado.length() > 500) {
            throw new RegraNegocioException("Checkpoint excede o limite de 500 caracteres");
        }
        return normalizado;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getContaFinanceiraId() { return contaFinanceiraId; }
    public String getProvedor() { return provedor; }
    public String getIdentificadorExterno() { return identificadorExterno; }
    public String getCheckpoint() { return checkpoint; }
    public Instant getSincronizadoEm() { return sincronizadoEm; }
    public boolean isAtivo() { return ativo; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public long getVersao() { return versao; }
}
