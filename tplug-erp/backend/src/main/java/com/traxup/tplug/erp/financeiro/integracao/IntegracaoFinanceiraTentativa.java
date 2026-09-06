package com.traxup.tplug.erp.financeiro.integracao;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integracoes_financeiras_tentativas")
public class IntegracaoFinanceiraTentativa {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "integracao_id", nullable = false) private UUID integracaoId;
    @Column(nullable = false, length = 40) private String provedor;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "quantidade_lancamentos", nullable = false) private int quantidadeLancamentos;
    @Column(name = "duracao_ms", nullable = false) private long duracaoMs;
    @Column(name = "erro_codigo", length = 120) private String erroCodigo;
    @Column(name = "iniciado_em", nullable = false) private Instant iniciadoEm;
    @Column(name = "finalizado_em", nullable = false) private Instant finalizadoEm;

    protected IntegracaoFinanceiraTentativa() {}

    private IntegracaoFinanceiraTentativa(UUID tenantId, UUID integracaoId, String provedor, String status,
                                          int quantidadeLancamentos, long duracaoMs, String erroCodigo,
                                          Instant iniciadoEm, Instant finalizadoEm) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.integracaoId = integracaoId;
        this.provedor = provedor; this.status = status; this.quantidadeLancamentos = quantidadeLancamentos;
        this.duracaoMs = Math.max(0, duracaoMs); this.erroCodigo = erroCodigo;
        this.iniciadoEm = iniciadoEm; this.finalizadoEm = finalizadoEm;
    }

    public static IntegracaoFinanceiraTentativa sucesso(UUID tenantId, UUID integracaoId, String provedor,
            int quantidade, long duracaoMs, Instant iniciadoEm, Instant finalizadoEm) {
        return new IntegracaoFinanceiraTentativa(tenantId, integracaoId, provedor, "SUCESSO", quantidade,
                duracaoMs, null, iniciadoEm, finalizadoEm);
    }

    public static IntegracaoFinanceiraTentativa falha(UUID tenantId, UUID integracaoId, String provedor,
            int quantidade, long duracaoMs, Throwable erro, Instant iniciadoEm, Instant finalizadoEm) {
        String codigo = erro == null ? "ERRO_DESCONHECIDO" : erro.getClass().getSimpleName();
        if (codigo.length() > 120) codigo = codigo.substring(0, 120);
        return new IntegracaoFinanceiraTentativa(tenantId, integracaoId, provedor, "FALHA", quantidade,
                duracaoMs, codigo, iniciadoEm, finalizadoEm);
    }

    public UUID getId() { return id; } public UUID getTenantId() { return tenantId; }
    public UUID getIntegracaoId() { return integracaoId; } public String getProvedor() { return provedor; }
    public String getStatus() { return status; } public int getQuantidadeLancamentos() { return quantidadeLancamentos; }
    public long getDuracaoMs() { return duracaoMs; } public String getErroCodigo() { return erroCodigo; }
    public Instant getIniciadoEm() { return iniciadoEm; } public Instant getFinalizadoEm() { return finalizadoEm; }
}
