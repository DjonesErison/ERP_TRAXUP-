CREATE TABLE produtos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    codigo VARCHAR(60) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    grupo VARCHAR(120),
    ncm VARCHAR(8),
    venda_prc NUMERIC(19,4) NOT NULL DEFAULT 0,
    compra_prc NUMERIC(19,4) NOT NULL DEFAULT 0,
    codigo_barra VARCHAR(60),
    unidade VARCHAR(10) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_produtos_tenant_codigo UNIQUE (tenant_id, codigo)
);

CREATE INDEX idx_produtos_tenant_descricao ON produtos (tenant_id, descricao);
CREATE INDEX idx_produtos_tenant_codigo_barra ON produtos (tenant_id, codigo_barra);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('00000000-0000-0000-0000-000000000011', 'PRODUTO_LER', 'Consultar produtos'),
    ('00000000-0000-0000-0000-000000000012', 'PRODUTO_CRIAR', 'Criar produtos'),
    ('00000000-0000-0000-0000-000000000013', 'PRODUTO_DESATIVAR', 'Desativar produtos')
ON CONFLICT (chave) DO NOTHING;
