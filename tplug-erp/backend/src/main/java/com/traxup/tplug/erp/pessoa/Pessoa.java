package com.traxup.tplug.erp.pessoa;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pessoas")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    private String tipoPessoa;

    @Column(name = "nome_razao_social", nullable = false, length = 255)
    private String nomeRazaoSocial;

    @Column(name = "nome_fantasia", length = 255)
    private String nomeFantasia;

    @Column(name = "cpf_cnpj", length = 14)
    private String cpfCnpj;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "cliente", nullable = false)
    private boolean cliente;

    @Column(name = "fornecedor", nullable = false)
    private boolean fornecedor;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Pessoa() {}

    public Pessoa(Tenant tenant, String tipoPessoa, String nomeRazaoSocial, String nomeFantasia,
                  String cpfCnpj, String email, String telefone, boolean cliente, boolean fornecedor) {
        this.tenant = tenant;
        this.tipoPessoa = tipoPessoa;
        this.nomeRazaoSocial = nomeRazaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.telefone = telefone;
        this.cliente = cliente;
        this.fornecedor = fornecedor;
        this.ativo = true;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() { atualizadoEm = Instant.now(); }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public String getTipoPessoa() { return tipoPessoa; }
    public String getNomeRazaoSocial() { return nomeRazaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public String getCpfCnpj() { return cpfCnpj; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public boolean isCliente() { return cliente; }
    public boolean isFornecedor() { return fornecedor; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void desativar() { this.ativo = false; }
}
