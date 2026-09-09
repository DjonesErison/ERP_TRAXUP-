package com.traxup.tplug.erp.pdv;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pdv_caixa_sessoes")
public class PdvCaixaSessao {
    @Id private UUID id;
    @Column(name="tenant_id", nullable=false) private UUID tenantId;
    @Column(name="filial_id", nullable=false) private UUID filialId;
    @Column(name="terminal_id", nullable=false) private UUID terminalId;
    @Column(name="conta_financeira_id", nullable=false) private UUID contaFinanceiraId;
    @Column(name="usuario_abertura_id") private UUID usuarioAberturaId;
    @Column(name="usuario_fechamento_id") private UUID usuarioFechamentoId;
    @Column(nullable=false, length=16) private String status;
    @Column(name="saldo_abertura", nullable=false, precision=19, scale=4) private BigDecimal saldoAbertura;
    @Column(name="saldo_sistema_fechamento", precision=19, scale=4) private BigDecimal saldoSistemaFechamento;
    @Column(name="saldo_informado_fechamento", precision=19, scale=4) private BigDecimal saldoInformadoFechamento;
    @Column(name="diferenca_fechamento", precision=19, scale=4) private BigDecimal diferencaFechamento;
    @Column(name="aberto_em", nullable=false) private Instant abertoEm;
    @Column(name="fechado_em") private Instant fechadoEm;
    @Column(name="observacao_fechamento", length=500) private String observacaoFechamento;

    protected PdvCaixaSessao() {}

    public PdvCaixaSessao(UUID tenantId, UUID filialId, UUID terminalId, UUID contaFinanceiraId,
                          UUID usuarioAberturaId, BigDecimal saldoAbertura) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.terminalId = terminalId;
        this.contaFinanceiraId = contaFinanceiraId;
        this.usuarioAberturaId = usuarioAberturaId;
        this.status = "ABERTO";
        this.saldoAbertura = saldoAbertura;
        this.abertoEm = Instant.now();
    }

    public void fechar(UUID usuarioId, BigDecimal saldoSistema, BigDecimal saldoInformado, String observacao) {
        if (!"ABERTO".equals(status)) throw new IllegalArgumentException("Sessao de caixa nao esta aberta");
        if (saldoInformado == null || saldoInformado.signum() < 0) throw new IllegalArgumentException("Saldo informado deve ser maior ou igual a zero");
        this.usuarioFechamentoId = usuarioId;
        this.saldoSistemaFechamento = saldoSistema;
        this.saldoInformadoFechamento = saldoInformado;
        this.diferencaFechamento = saldoInformado.subtract(saldoSistema);
        this.observacaoFechamento = observacao == null || observacao.isBlank() ? null : observacao.trim();
        this.fechadoEm = Instant.now();
        this.status = "FECHADO";
    }

    public UUID getId(){return id;} public UUID getTenantId(){return tenantId;} public UUID getFilialId(){return filialId;}
    public UUID getTerminalId(){return terminalId;} public UUID getContaFinanceiraId(){return contaFinanceiraId;}
    public UUID getUsuarioAberturaId(){return usuarioAberturaId;} public UUID getUsuarioFechamentoId(){return usuarioFechamentoId;}
    public String getStatus(){return status;} public BigDecimal getSaldoAbertura(){return saldoAbertura;}
    public BigDecimal getSaldoSistemaFechamento(){return saldoSistemaFechamento;} public BigDecimal getSaldoInformadoFechamento(){return saldoInformadoFechamento;}
    public BigDecimal getDiferencaFechamento(){return diferencaFechamento;} public Instant getAbertoEm(){return abertoEm;}
    public Instant getFechadoEm(){return fechadoEm;} public String getObservacaoFechamento(){return observacaoFechamento;}
}
