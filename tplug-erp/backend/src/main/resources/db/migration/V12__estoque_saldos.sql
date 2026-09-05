CREATE TABLE estoque_saldos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    tipo_item VARCHAR(10) NOT NULL,
    item_id UUID NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_estoque_saldos_tipo_item CHECK (tipo_item IN ('PRODUTO', 'GRADE')),
    CONSTRAINT uk_estoque_saldos_item UNIQUE (tenant_id, filial_id, tipo_item, item_id)
);

CREATE INDEX idx_estoque_saldos_tenant_filial ON estoque_saldos (tenant_id, filial_id);

INSERT INTO permissoes (id, chave, descricao)
VALUES ('10000000-0000-0000-0000-000000000018', 'ESTOQUE_LER', 'Permite consultar saldos de estoque do tenant')
ON CONFLICT (chave) DO NOTHING;
