package com.traxup.tplug.erp.fiscal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fiscal_perfis_filial")
public class FiscalPerfilFilial {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "regime_tributario", nullable = false, length = 20) private String regimeTributario;
    @Column(nullable = false) private short crt;
    @Column(nullable = false, length = 20) private String ambiente;
    @Column(name = "serie_nfe", nullable = false) private int serieNfe;
    @Column(name = "serie_nfce", nullable = false) private int serieNfce;
    @Column(nullable = false) private boolean ativo;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected FiscalPerfilFilial() {}

    public FiscalPerfilFilial(UUID tenantId, UUID filialId, String regimeTributario,
                              short crt, String ambiente, int serieNfe, int serieNfce) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        atualizar(regimeTributario, crt, ambiente, serieNfe, serieNfce);
        this.ativo = true;
    }

    public void atualizar(String regimeTributario, short crt, String ambiente,
                          int serieNfe, int serieNfce) {
        this.regimeTributario = regimeTributario;
        this.crt = crt;
        this.ambiente = ambiente;
        this.serieNfe = serieNfe;
        this.serieNfce = serieNfce;
        this.ativo = true;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        criadoEm = agora;
        atualizadoEm = agora;
    }
    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public String getRegimeTributario() { return regimeTributario; }
    public short getCrt() { return crt; }
    public String getAmbiente() { return ambiente; }
    public int getSerieNfe() { return serieNfe; }
    public int getSerieNfce() { return serieNfce; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
