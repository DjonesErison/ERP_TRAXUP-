package com.traxup.tplug.erp.venda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pedido_venda_itens")
public class PedidoVendaItem {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "pedido_venda_id", nullable = false)
    private UUID pedidoVendaId;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precoUnitario;

    @Column(name = "adicional_combo_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal adicionalComboUnitario;

    @Column(name = "desconto_valor", nullable = false, precision = 19, scale = 4)
    private BigDecimal descontoValor;

    @Column(name = "total_item", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalItem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PedidoVendaItem() {}

    public PedidoVendaItem(UUID tenantId, UUID pedidoVendaId, UUID produtoId, UUID gradeId,
                           BigDecimal quantidade, BigDecimal precoUnitario) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.pedidoVendaId = pedidoVendaId;
        this.produtoId = produtoId;
        this.gradeId = gradeId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.adicionalComboUnitario = BigDecimal.ZERO;
        this.descontoValor = BigDecimal.ZERO;
        recalcularTotal();
    }

    public void aplicarDesconto(BigDecimal descontoValor) {
        BigDecimal bruto = quantidade.multiply(precoUnitario.add(adicionalComboUnitario));
        if (descontoValor == null || descontoValor.signum() < 0) {
            throw new IllegalArgumentException("Desconto nao pode ser negativo");
        }
        if (descontoValor.compareTo(bruto) > 0) {
            throw new IllegalArgumentException("Desconto nao pode ser maior que o total bruto do item");
        }
        this.descontoValor = descontoValor;
        recalcularTotal();
    }

    public void definirAdicionalComboUnitario(BigDecimal adicionalComboUnitario) {
        if (adicionalComboUnitario == null || adicionalComboUnitario.signum() < 0) {
            throw new IllegalArgumentException("Adicional do combo nao pode ser negativo");
        }
        this.adicionalComboUnitario = adicionalComboUnitario;
        recalcularTotal();
    }

    @PrePersist
    void prePersist() {
        Instant agora = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (adicionalComboUnitario == null) adicionalComboUnitario = BigDecimal.ZERO;
        if (descontoValor == null) descontoValor = BigDecimal.ZERO;
        recalcularTotal();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        if (adicionalComboUnitario == null) adicionalComboUnitario = BigDecimal.ZERO;
        if (descontoValor == null) descontoValor = BigDecimal.ZERO;
        recalcularTotal();
        atualizadoEm = Instant.now();
    }

    private void recalcularTotal() {
        BigDecimal adicional = adicionalComboUnitario == null ? BigDecimal.ZERO : adicionalComboUnitario;
        BigDecimal desconto = descontoValor == null ? BigDecimal.ZERO : descontoValor;
        totalItem = quantidade.multiply(precoUnitario.add(adicional)).subtract(desconto);
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getPedidoVendaId() { return pedidoVendaId; }
    public UUID getProdutoId() { return produtoId; }
    public UUID getGradeId() { return gradeId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getAdicionalComboUnitario() { return adicionalComboUnitario; }
    public BigDecimal getDescontoValor() { return descontoValor; }
    public BigDecimal getTotalItem() { return totalItem; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
