CREATE TABLE estoque_movimentacoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    tipo_item VARCHAR(10) NOT NULL,
    item_id UUID NOT NULL,
    tipo_movimento VARCHAR(10) NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    saldo_anterior NUMERIC(19,4) NOT NULL,
    saldo_posterior NUMERIC(19,4) NOT NULL,
    motivo VARCHAR(255),
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_estoque_mov_tipo_item CHECK (tipo_item IN ('PRODUTO', 'GRADE')),
    CONSTRAINT ck_estoque_mov_tipo_movimento CHECK (tipo_movimento IN ('ENTRADA', 'SAIDA', 'AJUSTE')),
    CONSTRAINT ck_estoque_mov_quantidade CHECK (quantidade >= 0)
);

CREATE INDEX idx_estoque_mov_tenant_filial_criado ON estoque_movimentacoes (tenant_id, filial_id, criado_em DESC);
CREATE INDEX idx_estoque_mov_item ON estoque_movimentacoes (tenant_id, filial_id, tipo_item, item_id, criado_em DESC);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000019', 'ESTOQUE_MOVIMENTAR', 'Permite registrar entradas, saidas e ajustes de estoque'),
    ('10000000-0000-0000-0000-000000000020', 'ESTOQUE_MOVIMENTO_LER', 'Permite consultar historico de movimentacoes de estoque')
ON CONFLICT (chave) DO NOTHING;
