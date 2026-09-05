CREATE TABLE pedido_compra_itens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    pedido_compra_id UUID NOT NULL REFERENCES pedidos_compra(id),
    tipo_item VARCHAR(10) NOT NULL,
    item_id UUID NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    preco_unitario NUMERIC(19,4) NOT NULL,
    total_item NUMERIC(19,4) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pedido_compra_itens_tipo CHECK (tipo_item IN ('PRODUTO', 'GRADE')),
    CONSTRAINT ck_pedido_compra_itens_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_pedido_compra_itens_preco CHECK (preco_unitario >= 0),
    CONSTRAINT ck_pedido_compra_itens_total CHECK (total_item >= 0)
);

CREATE INDEX idx_pedido_compra_itens_tenant_pedido
    ON pedido_compra_itens (tenant_id, pedido_compra_id);

INSERT INTO permissoes (id, chave, descricao)
VALUES ('10000000-0000-0000-0000-000000000032', 'COMPRA_PEDIDO_EDITAR', 'Gerenciar itens de pedidos de compra')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
CROSS JOIN permissoes perm
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
