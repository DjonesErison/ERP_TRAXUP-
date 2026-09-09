package com.traxup.tplug.erp.pdv;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pdv_configuracoes")
public class PdvConfiguracao {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "terminal_id", nullable = false) private UUID terminalId;
    @Column(name = "exigir_justificativa_cancelamento", nullable = false) private boolean exigirJustificativaCancelamento;
    @Column(name = "exigir_autorizacao_cancelamento", nullable = false) private boolean exigirAutorizacaoCancelamento;
    @Column(name = "tamanho_impressao", nullable = false, length = 16) private String tamanhoImpressao;
    @Column(name = "imprimir_caixa", nullable = false) private boolean imprimirCaixa;
    @Column(name = "imprimir_cozinha", nullable = false) private boolean imprimirCozinha;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected PdvConfiguracao() {}

    public PdvConfiguracao(UUID tenantId, UUID filialId, UUID terminalId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.terminalId = terminalId;
        this.exigirJustificativaCancelamento = true;
        this.exigirAutorizacaoCancelamento = false;
        this.tamanhoImpressao = "MEDIA";
        this.imprimirCaixa = true;
        this.imprimirCozinha = false;
    }

    public void atualizar(boolean exigirJustificativa, boolean exigirAutorizacao, String tamanho,
                          boolean imprimirCaixa, boolean imprimirCozinha) {
        this.exigirJustificativaCancelamento = exigirJustificativa;
        this.exigirAutorizacaoCancelamento = exigirAutorizacao;
        this.tamanhoImpressao = tamanho;
        this.imprimirCaixa = imprimirCaixa;
        this.imprimirCozinha = imprimirCozinha;
    }

    @PrePersist void prePersist() { Instant agora = Instant.now(); if (id == null) id = UUID.randomUUID(); criadoEm = agora; atualizadoEm = agora; }
    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getTerminalId() { return terminalId; }
    public boolean isExigirJustificativaCancelamento() { return exigirJustificativaCancelamento; }
    public boolean isExigirAutorizacaoCancelamento() { return exigirAutorizacaoCancelamento; }
    public String getTamanhoImpressao() { return tamanhoImpressao; }
    public boolean isImprimirCaixa() { return imprimirCaixa; }
    public boolean isImprimirCozinha() { return imprimirCozinha; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
