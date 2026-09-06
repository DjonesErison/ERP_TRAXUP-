package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contas_receber")
public class ContaReceber {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "cliente_id", nullable = false) private UUID clienteId;
    @Column(name = "numero_documento", nullable = false, length = 60) private String numeroDocumento;
    @Column(nullable = false, length = 200) private String descricao;
    @Column(name = "valor_original", nullable = false, precision = 19, scale = 4) private BigDecimal valorOriginal;
    @Column(name = "valor_recebido", nullable = false, precision = 19, scale = 4) private BigDecimal valorRecebido;
    @Column(nullable = false) private LocalDate vencimento;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "origem_tipo", length = 40) private String origemTipo;
    @Column(name = "origem_id") private UUID origemId;
    @Column(name = "origem_referencia", length = 80) private String origemReferencia;
    @Column(name = "recebido_em") private Instant recebidoEm;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;
    @Version
    @Column(nullable = false)
    private long versao;

    protected ContaReceber() {}

    public ContaReceber(UUID tenantId, UUID filialId, UUID clienteId, String numeroDocumento,
                        String descricao, BigDecimal valorOriginal, LocalDate vencimento, UUID usuarioId) {
        this(tenantId, filialId, clienteId, numeroDocumento, descricao, valorOriginal, vencimento, usuarioId,
                null, null, null);
    }

    public ContaReceber(UUID tenantId, UUID filialId, UUID clienteId, String numeroDocumento,
                        String descricao, BigDecimal valorOriginal, LocalDate vencimento, UUID usuarioId,
                        String origemTipo, UUID origemId, String origemReferencia) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.clienteId = clienteId;
        this.numeroDocumento = numeroDocumento;
        this.descricao = descricao;
        this.valorOriginal = valorOriginal;
        this.valorRecebido = BigDecimal.ZERO;
        this.vencimento = vencimento;
        this.status = "ABERTO";
        this.usuarioId = usuarioId;
        this.origemTipo = origemTipo;
        this.origemId = origemId;
        this.origemReferencia = origemReferencia;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (valorRecebido == null) valorRecebido = BigDecimal.ZERO;
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate void preUpdate() { atualizadoEm = Instant.now(); }

    public void receber() {
        receber(getSaldoAberto());
    }

    public void receber(BigDecimal valor) {
        if (!("ABERTO".equals(status) || "PARCIAL".equals(status))) {
            throw new RegraNegocioException("Somente conta ABERTA ou PARCIAL pode receber baixa");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Valor do recebimento deve ser maior que zero");
        }

        BigDecimal saldo = getSaldoAberto();
        if (valor.compareTo(saldo) > 0) {
            throw new RegraNegocioException("Valor do recebimento nao pode exceder o saldo aberto");
        }

        valorRecebido = valorRecebido.add(valor);
        if (valorRecebido.compareTo(valorOriginal) == 0) {
            status = "RECEBIDO";
            recebidoEm = Instant.now();
        } else {
            status = "PARCIAL";
            recebidoEm = null;
        }
    }

    public BigDecimal getSaldoAberto() {
        return valorOriginal.subtract(valorRecebido);
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
    public UUID getClienteId() { return clienteId; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public BigDecimal getValorRecebido() { return valorRecebido; }
    public LocalDate getVencimento() { return vencimento; }
    public String getStatus() { return status; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getOrigemTipo() { return origemTipo; }
    public UUID getOrigemId() { return origemId; }
    public String getOrigemReferencia() { return origemReferencia; }
    public Instant getRecebidoEm() { return recebidoEm; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public long getVersao() { return versao; }
}
