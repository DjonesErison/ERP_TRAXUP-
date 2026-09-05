package com.traxup.tplug.erp.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recebimentos_compra")
public class RecebimentoCompra {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "pedido_compra_id", nullable = false) private UUID pedidoCompraId;
    @Column(name = "filial_id", nullable = false) private UUID filialId;
    @Column(name = "fornecedor_id", nullable = false) private UUID fornecedorId;
    @Column(nullable = false, length = 30) private String status;
    @Column(length = 60) private String documento;
    @Column(length = 500) private String observacao;
    @Column(name = "usuario_id") private UUID usuarioId;
    @Column(name = "recebido_em", nullable = false) private Instant recebidoEm;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;

    protected RecebimentoCompra() {}

    public RecebimentoCompra(UUID tenantId, UUID pedidoCompraId, UUID filialId, UUID fornecedorId,
                             String documento, String observacao, UUID usuarioId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.pedidoCompraId = pedidoCompraId;
        this.filialId = filialId;
        this.fornecedorId = fornecedorId;
        this.status = "CONFERIDO";
        this.documento = documento;
        this.observacao = observacao;
        this.usuarioId = usuarioId;
    }

    @PrePersist void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        recebidoEm = agora;
        criadoEm = agora;
    }

    public void marcarIntegradoEstoque() {
        if (!"CONFERIDO".equals(status)) {
            throw new IllegalStateException("Recebimento precisa estar CONFERIDO para integrar ao estoque");
        }
        status = "INTEGRADO_ESTOQUE";
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getPedidoCompraId() { return pedidoCompraId; }
    public UUID getFilialId() { return filialId; }
    public UUID getFornecedorId() { return fornecedorId; }
    public String getStatus() { return status; }
    public String getDocumento() { return documento; }
    public String getObservacao() { return observacao; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getRecebidoEm() { return recebidoEm; }
    public Instant getCriadoEm() { return criadoEm; }
}
