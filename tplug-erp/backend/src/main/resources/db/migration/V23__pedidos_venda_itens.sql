CREATE TABLE pedido_venda_itens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    pedido_venda_id UUID NOT NULL REFERENCES pedidos_venda(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produtos(id),
    grade_id UUID REFERENCES grades_produto(id),
    quantidade NUMERIC(19,4) NOT NULL,
    preco_unitario NUMERIC(19,4) NOT NULL,
    total_item NUMERIC(19,4) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pedido_venda_itens_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_pedido_venda_itens_preco CHECK (preco_unitario >= 0),
    CONSTRAINT ck_pedido_venda_itens_total CHECK (total_item >= 0)
);

CREATE INDEX idx_pedido_venda_itens_tenant_pedido ON pedido_venda_itens (tenant_id, pedido_venda_id);
CREATE INDEX idx_pedido_venda_itens_tenant_produto ON pedido_venda_itens (tenant_id, produto_id);
