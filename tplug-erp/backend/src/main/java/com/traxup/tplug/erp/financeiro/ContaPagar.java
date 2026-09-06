package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contas_pagar")
public class ContaPagar {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "fornecedor_id", nullable = false) private UUID fornecedorId;
    @Column(name = "numero_documento", nullable = false, length = 60) private String numeroDocumento;
    @Column(nullable = false, length = 200) private String descricao;
    @Column(name = "valor_original", nullable = false, precision = 19, scale = 4) private BigDecimal valorOriginal;
    @Column(name = "valor_pago", nullable = false, precision = 19, scale = 4) private BigDecimal valorPago;
    @Column(nullable = false) private LocalDate vencimento;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "pago_em") private Instant pagoEm;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;
    @Version @Column(nullable = false) private long versao;

    protected ContaPagar() {}

    public ContaPagar(UUID tenantId, UUID filialId, UUID fornecedorId, String numeroDocumento,
                      String descricao, BigDecimal valorOriginal, LocalDate vencimento, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.fornecedorId = fornecedorId;
        this.numeroDocumento = numeroDocumento;
        this.descricao = descricao;
        this.valorOriginal = valorOriginal;
        this.valorPago = BigDecimal.ZERO;
        this.vencimento = vencimento;
        this.status = "ABERTO";
        this.usuarioId = usuarioId;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (valorPago == null) valorPago = BigDecimal.ZERO;
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public void pagar() {
        pagar(getSaldoAberto());
    }

    public void pagar(BigDecimal valor) {
        if (!("ABERTO".equals(status) || "PARCIAL".equals(status))) {
            throw new RegraNegocioException("Somente conta ABERTA ou PARCIAL pode receber pagamento");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Valor do pagamento deve ser maior que zero");
        }
        BigDecimal saldo = getSaldoAberto();
        if (valor.compareTo(saldo) > 0) {
            throw new RegraNegocioException("Valor do pagamento nao pode exceder o saldo aberto");
        }
        valorPago = valorPago.add(valor);
        if (valorPago.compareTo(valorOriginal) == 0) {
            status = "PAGO";
            pagoEm = Instant.now();
        } else {
            status = "PARCIAL";
            pagoEm = null;
        }
    }

    public BigDecimal getSaldoAberto() {
        return valorOriginal.subtract(valorPago);
    }

    public void cancelar() {
        if (!"ABERTO".equals(status)) {
            throw new RegraNegocioException("Somente conta ABERTA pode ser cancelada");
        }
        status = "CANCELADO";
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getFornecedorId() { return fornecedorId; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public BigDecimal getValorPago() { return valorPago; }
    public LocalDate getVencimento() { return vencimento; }
    public String getStatus() { return status; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getPagoEm() { return pagoEm; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public long getVersao() { return versao; }
}
