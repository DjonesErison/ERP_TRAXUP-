CREATE TABLE pedidos_venda (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    cliente_id UUID REFERENCES pessoas(id),
    numero VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    observacao VARCHAR(500),
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pedidos_venda_status CHECK (status IN ('RASCUNHO', 'ABERTO', 'CANCELADO', 'FATURADO')),
    CONSTRAINT uk_pedidos_venda_tenant_numero UNIQUE (tenant_id, numero)
);

CREATE INDEX idx_pedidos_venda_tenant_criado ON pedidos_venda (tenant_id, criado_em DESC);
CREATE INDEX idx_pedidos_venda_tenant_filial ON pedidos_venda (tenant_id, filial_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000036', 'VENDA_PEDIDO_LER', 'Consultar pedidos de venda'),
    ('10000000-0000-0000-0000-000000000037', 'VENDA_PEDIDO_CRIAR', 'Criar pedidos de venda'),
    ('10000000-0000-0000-0000-000000000038', 'VENDA_PEDIDO_EDITAR', 'Alterar ciclo de vida de pedidos de venda')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('VENDA_PEDIDO_LER', 'VENDA_PEDIDO_CRIAR', 'VENDA_PEDIDO_EDITAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
