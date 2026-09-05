package com.traxup.tplug.erp.produto.grade;

import com.traxup.tplug.erp.produto.Produto;
import com.traxup.tplug.erp.tenant.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grades_produto")
public class GradeProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "codigo_grade", nullable = false, length = 60)
    private String codigoGrade;

    @Column(name = "descricao_grade", nullable = false, length = 255)
    private String descricaoGrade;

    @Column(name = "codigo_barra", length = 60)
    private String codigoBarra;

    @Column(name = "venda_prc", precision = 19, scale = 4)
    private BigDecimal vendaPrc;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected GradeProduto() {
    }

    public GradeProduto(Tenant tenant, Produto produto, String codigoGrade, String descricaoGrade,
                        String codigoBarra, BigDecimal vendaPrc) {
        this.tenant = tenant;
        this.produto = produto;
        this.codigoGrade = codigoGrade;
        this.descricaoGrade = descricaoGrade;
        this.codigoBarra = codigoBarra;
        this.vendaPrc = vendaPrc;
        this.ativo = true;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public Produto getProduto() { return produto; }
    public String getCodigoGrade() { return codigoGrade; }
    public String getDescricaoGrade() { return descricaoGrade; }
    public String getCodigoBarra() { return codigoBarra; }
    public BigDecimal getVendaPrc() { return vendaPrc; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void desativar() {
        this.ativo = false;
    }
}
