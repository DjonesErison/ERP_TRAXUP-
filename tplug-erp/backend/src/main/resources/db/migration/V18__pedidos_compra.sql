CREATE TABLE pedidos_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    fornecedor_id UUID NOT NULL REFERENCES pessoas(id),
    numero VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    observacao VARCHAR(500),
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pedidos_compra_status CHECK (status IN ('RASCUNHO', 'ABERTO', 'CANCELADO', 'RECEBIDO')),
    CONSTRAINT uk_pedidos_compra_tenant_numero UNIQUE (tenant_id, numero)
);

CREATE INDEX idx_pedidos_compra_tenant_filial ON pedidos_compra (tenant_id, filial_id);
CREATE INDEX idx_pedidos_compra_tenant_fornecedor ON pedidos_compra (tenant_id, fornecedor_id);
CREATE INDEX idx_pedidos_compra_tenant_status ON pedidos_compra (tenant_id, status);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000030', 'COMPRA_PEDIDO_LER', 'Consultar pedidos de compra'),
    ('10000000-0000-0000-0000-000000000031', 'COMPRA_PEDIDO_CRIAR', 'Criar pedidos de compra')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
CROSS JOIN permissoes perm
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
