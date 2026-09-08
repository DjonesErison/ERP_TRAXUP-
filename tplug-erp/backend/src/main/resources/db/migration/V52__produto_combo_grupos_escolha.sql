CREATE TABLE produto_combo_grupos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    combo_produto_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    minimo_escolhas INTEGER NOT NULL DEFAULT 1,
    maximo_escolhas INTEGER NOT NULL DEFAULT 1,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_produto_combo_grupo_minimo CHECK (minimo_escolhas >= 0),
    CONSTRAINT ck_produto_combo_grupo_maximo CHECK (maximo_escolhas >= 1),
    CONSTRAINT ck_produto_combo_grupo_intervalo CHECK (minimo_escolhas <= maximo_escolhas),
    CONSTRAINT uk_produto_combo_grupo_nome UNIQUE (tenant_id, combo_produto_id, nome),
    CONSTRAINT fk_produto_combo_grupo_produto_tenant
        FOREIGN KEY (combo_produto_id, tenant_id) REFERENCES produtos(id, tenant_id)
);

CREATE TABLE produto_combo_grupo_opcoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    grupo_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL DEFAULT 1,
    valor_adicional NUMERIC(19,4) NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_produto_combo_opcao_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_produto_combo_opcao_adicional CHECK (valor_adicional >= 0),
    CONSTRAINT uk_produto_combo_grupo_opcao UNIQUE (tenant_id, grupo_id, produto_id),
    CONSTRAINT fk_produto_combo_opcao_grupo_tenant
        FOREIGN KEY (grupo_id, tenant_id) REFERENCES produto_combo_grupos(id, tenant_id),
    CONSTRAINT fk_produto_combo_opcao_produto_tenant
        FOREIGN KEY (produto_id, tenant_id) REFERENCES produtos(id, tenant_id)
);

ALTER TABLE produto_combo_grupos
    ADD CONSTRAINT uk_produto_combo_grupos_id_tenant UNIQUE (id, tenant_id);

CREATE INDEX idx_produto_combo_grupos_tenant_combo
    ON produto_combo_grupos (tenant_id, combo_produto_id);
CREATE INDEX idx_produto_combo_opcoes_tenant_grupo
    ON produto_combo_grupo_opcoes (tenant_id, grupo_id);
