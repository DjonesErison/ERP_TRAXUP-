CREATE TABLE contas_receber (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    cliente_id UUID NOT NULL REFERENCES pessoas(id),
    numero_documento VARCHAR(60) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    valor_original NUMERIC(19,4) NOT NULL,
    valor_recebido NUMERIC(19,4) NOT NULL DEFAULT 0,
    vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    usuario_id UUID,
    recebido_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_contas_receber_valor_original CHECK (valor_original > 0),
    CONSTRAINT ck_contas_receber_valor_recebido CHECK (valor_recebido >= 0 AND valor_recebido <= valor_original),
    CONSTRAINT ck_contas_receber_status CHECK (status IN ('ABERTO', 'RECEBIDO', 'CANCELADO'))
);

CREATE INDEX idx_contas_receber_tenant_vencimento ON contas_receber (tenant_id, vencimento);
CREATE INDEX idx_contas_receber_tenant_status ON contas_receber (tenant_id, status);
CREATE INDEX idx_contas_receber_tenant_cliente ON contas_receber (tenant_id, cliente_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000045', 'FINANCEIRO_RECEBER_LER', 'Consultar contas a receber'),
    ('10000000-0000-0000-0000-000000000046', 'FINANCEIRO_RECEBER_CRIAR', 'Criar contas a receber'),
    ('10000000-0000-0000-0000-000000000047', 'FINANCEIRO_RECEBER_BAIXAR', 'Registrar recebimento financeiro'),
    ('10000000-0000-0000-0000-000000000048', 'FINANCEIRO_RECEBER_CANCELAR', 'Cancelar contas a receber')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN (
    'FINANCEIRO_RECEBER_LER',
    'FINANCEIRO_RECEBER_CRIAR',
    'FINANCEIRO_RECEBER_BAIXAR',
    'FINANCEIRO_RECEBER_CANCELAR'
)
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
