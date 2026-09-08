ALTER TABLE produtos
    ADD CONSTRAINT uk_produtos_id_tenant UNIQUE (id, tenant_id);

CREATE TABLE produto_combo_componentes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    combo_produto_id UUID NOT NULL,
    componente_produto_id UUID NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_produto_combo_quantidade_positiva CHECK (quantidade > 0),
    CONSTRAINT ck_produto_combo_sem_autorreferencia CHECK (combo_produto_id <> componente_produto_id),
    CONSTRAINT uk_produto_combo_componente UNIQUE (tenant_id, combo_produto_id, componente_produto_id),
    CONSTRAINT fk_produto_combo_produto_tenant
        FOREIGN KEY (combo_produto_id, tenant_id) REFERENCES produtos(id, tenant_id),
    CONSTRAINT fk_produto_combo_componente_tenant
        FOREIGN KEY (componente_produto_id, tenant_id) REFERENCES produtos(id, tenant_id)
);

CREATE INDEX idx_produto_combo_tenant_combo
    ON produto_combo_componentes (tenant_id, combo_produto_id);
CREATE INDEX idx_produto_combo_tenant_componente
    ON produto_combo_componentes (tenant_id, componente_produto_id);
