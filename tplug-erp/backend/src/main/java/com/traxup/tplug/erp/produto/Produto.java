package com.traxup.tplug.erp.produto;

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
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "grupo", length = 120)
    private String grupo;

    @Column(name = "ncm", length = 8)
    private String ncm;

    @Column(name = "venda_prc", nullable = false, precision = 19, scale = 4)
    private BigDecimal vendaPrc;

    @Column(name = "compra_prc", nullable = false, precision = 19, scale = 4)
    private BigDecimal compraPrc;

    @Column(name = "codigo_barra", length = 60)
    private String codigoBarra;

    @Column(name = "unidade", nullable = false, length = 10)
    private String unidade;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Produto() {
    }

    public Produto(Tenant tenant, String codigo, String descricao, String grupo, String ncm,
                   BigDecimal vendaPrc, BigDecimal compraPrc, String codigoBarra, String unidade) {
        this.tenant = tenant;
        this.codigo = codigo;
        this.descricao = descricao;
        this.grupo = grupo;
        this.ncm = ncm;
        this.vendaPrc = vendaPrc;
        this.compraPrc = compraPrc;
        this.codigoBarra = codigoBarra;
        this.unidade = unidade;
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
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public String getGrupo() { return grupo; }
    public String getNcm() { return ncm; }
    public BigDecimal getVendaPrc() { return vendaPrc; }
    public BigDecimal getCompraPrc() { return compraPrc; }
    public String getCodigoBarra() { return codigoBarra; }
    public String getUnidade() { return unidade; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void desativar() {
        this.ativo = false;
    }
}
