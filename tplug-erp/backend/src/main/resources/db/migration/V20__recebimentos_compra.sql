CREATE TABLE recebimentos_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    pedido_compra_id UUID NOT NULL REFERENCES pedidos_compra(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    fornecedor_id UUID NOT NULL REFERENCES pessoas(id),
    status VARCHAR(30) NOT NULL,
    documento VARCHAR(60),
    observacao VARCHAR(500),
    usuario_id UUID,
    recebido_em TIMESTAMPTZ NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_recebimentos_compra_status CHECK (status IN ('CONFERIDO', 'INTEGRADO_ESTOQUE')),
    CONSTRAINT uk_recebimentos_compra_pedido UNIQUE (tenant_id, pedido_compra_id)
);

CREATE TABLE recebimento_compra_itens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    recebimento_id UUID NOT NULL REFERENCES recebimentos_compra(id) ON DELETE CASCADE,
    pedido_item_id UUID NOT NULL REFERENCES pedido_compra_itens(id),
    produto_id UUID NOT NULL REFERENCES produtos(id),
    grade_id UUID REFERENCES grades_produto(id),
    quantidade_pedida NUMERIC(19,4) NOT NULL,
    quantidade_recebida NUMERIC(19,4) NOT NULL,
    preco_unitario NUMERIC(19,4) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_recebimento_item_qtd_pedida CHECK (quantidade_pedida > 0),
    CONSTRAINT ck_recebimento_item_qtd_recebida CHECK (quantidade_recebida >= 0),
    CONSTRAINT ck_recebimento_item_preco CHECK (preco_unitario >= 0),
    CONSTRAINT uk_recebimento_item_pedido_item UNIQUE (tenant_id, recebimento_id, pedido_item_id)
);

CREATE INDEX idx_recebimentos_compra_tenant_pedido ON recebimentos_compra (tenant_id, pedido_compra_id);
CREATE INDEX idx_recebimento_itens_tenant_recebimento ON recebimento_compra_itens (tenant_id, recebimento_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000033', 'COMPRA_RECEBIMENTO_LER', 'Consultar recebimentos de compras'),
    ('10000000-0000-0000-0000-000000000034', 'COMPRA_RECEBIMENTO_REGISTRAR', 'Registrar conferencia de recebimento de compras')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('COMPRA_RECEBIMENTO_LER', 'COMPRA_RECEBIMENTO_REGISTRAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
