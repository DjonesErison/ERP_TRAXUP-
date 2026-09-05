package com.traxup.tplug.erp.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recebimento_compra_itens")
public class RecebimentoCompraItem {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "recebimento_id", nullable = false) private UUID recebimentoId;
    @Column(name = "pedido_item_id", nullable = false) private UUID pedidoItemId;
    @Column(name = "produto_id", nullable = false) private UUID produtoId;
    @Column(name = "grade_id") private UUID gradeId;
    @Column(name = "quantidade_pedida", nullable = false, precision = 19, scale = 4) private BigDecimal quantidadePedida;
    @Column(name = "quantidade_recebida", nullable = false, precision = 19, scale = 4) private BigDecimal quantidadeRecebida;
    @Column(name = "preco_unitario", nullable = false, precision = 19, scale = 4) private BigDecimal precoUnitario;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;

    protected RecebimentoCompraItem() {}
    public RecebimentoCompraItem(UUID tenantId, UUID recebimentoId, PedidoCompraItem item, BigDecimal quantidadeRecebida) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.recebimentoId = recebimentoId;
        this.pedidoItemId = item.getId(); this.produtoId = item.getProdutoId(); this.gradeId = item.getGradeId();
        this.quantidadePedida = item.getQuantidade(); this.quantidadeRecebida = quantidadeRecebida;
        this.precoUnitario = item.getPrecoUnitario();
    }
    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID(); criadoEm = Instant.now(); }
    public UUID getId() { return id; } public UUID getRecebimentoId() { return recebimentoId; }
    public UUID getPedidoItemId() { return pedidoItemId; } public UUID getProdutoId() { return produtoId; }
    public UUID getGradeId() { return gradeId; } public BigDecimal getQuantidadePedida() { return quantidadePedida; }
    public BigDecimal getQuantidadeRecebida() { return quantidadeRecebida; } public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
