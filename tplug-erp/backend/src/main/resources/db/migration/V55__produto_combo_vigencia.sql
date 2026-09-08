CREATE TABLE produto_combo_vigencias (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    combo_produto_id UUID NOT NULL,
    vigencia_inicio TIMESTAMPTZ,
    vigencia_fim TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_produto_combo_vigencia UNIQUE (tenant_id, combo_produto_id),
    CONSTRAINT ck_produto_combo_vigencia_intervalo CHECK (
        vigencia_inicio IS NULL OR vigencia_fim IS NULL OR vigencia_inicio <= vigencia_fim
    ),
    CONSTRAINT fk_produto_combo_vigencia_produto_tenant
        FOREIGN KEY (combo_produto_id, tenant_id) REFERENCES produtos(id, tenant_id)
);

CREATE INDEX idx_produto_combo_vigencia_tenant_combo
    ON produto_combo_vigencias (tenant_id, combo_produto_id);
