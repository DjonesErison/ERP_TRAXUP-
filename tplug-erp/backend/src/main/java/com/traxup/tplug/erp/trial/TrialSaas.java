package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.usuario.Usuario;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "trials_saas")
public class TrialSaas {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private Usuario administrador;

    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false, length = 14) private String documento;
    @Column(nullable = false, length = 20) private String telefone;
    @Column(length = 80) private String segmento;
    @Column(name = "quantidade_lojas", nullable = false) private int quantidadeLojas;
    @Column(name = "inicio_em", nullable = false) private Instant inicioEm;
    @Column(name = "expira_em", nullable = false) private Instant expiraEm;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "termos_versao", nullable = false, length = 40) private String termosVersao;
    @Column(name = "termos_aceitos_em", nullable = false) private Instant termosAceitosEm;
    @Column(name = "onboarding_status", nullable = false, length = 20) private String onboardingStatus;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100) private String idempotencyKey;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    @Column(name="admin_ativado_em") private Instant adminAtivadoEm;
    public boolean isAdminAtivado() { return adminAtivadoEm != null; }
    public void marcarAdminAtivado(Instant now) { adminAtivadoEm=now; }

    protected TrialSaas() {}

    public TrialSaas(Tenant tenant, Empresa empresa, Usuario administrador, String email, String documento,
                     String telefone, String segmento, int quantidadeLojas, String termosVersao,
                     String idempotencyKey, Instant agora) {
        if (quantidadeLojas < 1) throw new IllegalArgumentException("Quantidade de lojas invalida");
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.empresa = empresa;
        this.administrador = administrador;
        this.email = email;
        this.documento = documento;
        this.telefone = telefone;
        this.segmento = segmento;
        this.quantidadeLojas = quantidadeLojas;
        this.inicioEm = agora;
        this.expiraEm = agora.plus(7, ChronoUnit.DAYS);
        this.status = "ATIVO";
        this.termosVersao = termosVersao;
        this.termosAceitosEm = agora;
        this.onboardingStatus = "PENDENTE";
        this.idempotencyKey = idempotencyKey;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public UUID getId() { return id; }
    public String getCodigoEmpresa() { return tenant.getCodigoEmpresa(); }
    public UUID getTenantId() { return tenant.getId(); }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Usuario getAdministrador() { return administrador; }
    public Instant getExpiraEm() { return expiraEm; }
    public String getStatus() { return status; }
    public String getOnboardingStatus() { return onboardingStatus; }
}
