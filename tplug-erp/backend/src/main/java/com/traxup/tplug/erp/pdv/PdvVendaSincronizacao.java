package com.traxup.tplug.erp.pdv;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pdv_vendas_sincronizacao")
public class PdvVendaSincronizacao {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(name = "terminal_id", nullable = false)
    private UUID terminalId;
    @Column(name = "operacao_local_id", nullable = false)
    private UUID operacaoLocalId;
    @Column(nullable = false)
    private Integer serie;
    @Column(name = "numero_local", nullable = false)
    private Long numeroLocal;
    @Column(nullable = false, length = 64)
    private String checksum;
    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;
    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;
    @Column(name = "pedido_venda_id")
    private UUID pedidoVendaId;
    @Column(name = "processado_em")
    private Instant processadoEm;

    protected PdvVendaSincronizacao() {}

    public PdvVendaSincronizacao(UUID tenantId, UUID filialId, UUID terminalId, UUID operacaoLocalId,
                                 Integer serie, Long numeroLocal, String checksum, Instant ocorridoEm) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.filialId = filialId;
        this.terminalId = terminalId;
        this.operacaoLocalId = operacaoLocalId;
        this.serie = serie;
        this.numeroLocal = numeroLocal;
        this.checksum = checksum;
        this.ocorridoEm = ocorridoEm;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (recebidoEm == null) recebidoEm = Instant.now();
    }

    public boolean corresponde(Long numeroLocal, String checksum) {
        return this.numeroLocal.equals(numeroLocal) && this.checksum.equals(checksum);
    }

    public boolean isProcessada() { return pedidoVendaId != null && processadoEm != null; }

    public void marcarProcessada(UUID pedidoVendaId) {
        if (pedidoVendaId == null) throw new IllegalArgumentException("Pedido de venda e obrigatorio");
        if (isProcessada() && !this.pedidoVendaId.equals(pedidoVendaId)) {
            throw new IllegalStateException("Sincronizacao ja vinculada a outro pedido de venda");
        }
        this.pedidoVendaId = pedidoVendaId;
        if (this.processadoEm == null) this.processadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getFilialId() { return filialId; }
    public UUID getTerminalId() { return terminalId; }
    public UUID getOperacaoLocalId() { return operacaoLocalId; }
    public Integer getSerie() { return serie; }
    public Long getNumeroLocal() { return numeroLocal; }
    public String getChecksum() { return checksum; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public Instant getRecebidoEm() { return recebidoEm; }
    public UUID getPedidoVendaId() { return pedidoVendaId; }
    public Instant getProcessadoEm() { return processadoEm; }
}
