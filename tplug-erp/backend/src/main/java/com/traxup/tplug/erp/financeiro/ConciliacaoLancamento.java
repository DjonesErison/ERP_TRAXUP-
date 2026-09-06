package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conciliacao_lancamentos")
public class ConciliacaoLancamento {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "conta_financeira_id", nullable = false) private UUID contaFinanceiraId;
    @Column(nullable = false, length = 40) private String origem;
    @Column(name = "referencia_externa", nullable = false, length = 120) private String referenciaExterna;
    @Column(nullable = false, length = 20) private String tipo;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal valor;
    @Column(nullable = false, length = 200) private String descricao;
    @Column(name = "ocorrido_em", nullable = false) private Instant ocorridoEm;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "movimento_id") private UUID movimentoId;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "conciliado_em") private Instant conciliadoEm;

    protected ConciliacaoLancamento() {}

    public ConciliacaoLancamento(UUID tenantId, UUID filialId, UUID contaFinanceiraId, String origem,
                                 String referenciaExterna, String tipo, BigDecimal valor, String descricao,
                                 Instant ocorridoEm, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.contaFinanceiraId = contaFinanceiraId;
        this.origem = origem;
        this.referenciaExterna = referenciaExterna;
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.ocorridoEm = ocorridoEm;
        this.status = "PENDENTE";
        this.usuarioId = usuarioId;
    }

    @PrePersist void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (criadoEm == null) criadoEm = Instant.now();
    }

    public void conciliar(UUID movimentoId) {
        if (movimentoId == null) throw new RegraNegocioException("Movimento financeiro e obrigatorio");
        if (!"PENDENTE".equals(status)) {
            throw new RecursoConflitanteException("Lancamento ja conciliado");
        }
        this.movimentoId = movimentoId;
        this.status = "CONCILIADO";
        this.conciliadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getContaFinanceiraId() { return contaFinanceiraId; }
    public String getOrigem() { return origem; }
    public String getReferenciaExterna() { return referenciaExterna; }
    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public String getStatus() { return status; }
    public UUID getMovimentoId() { return movimentoId; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getConciliadoEm() { return conciliadoEm; }
}
