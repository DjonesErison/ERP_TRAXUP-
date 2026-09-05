CREATE TABLE contas_pagar (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    fornecedor_id UUID NOT NULL REFERENCES pessoas(id),
    numero_documento VARCHAR(60) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    valor_original NUMERIC(19,4) NOT NULL,
    valor_pago NUMERIC(19,4) NOT NULL DEFAULT 0,
    vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    usuario_id UUID,
    pago_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_contas_pagar_valor_original CHECK (valor_original > 0),
    CONSTRAINT ck_contas_pagar_valor_pago CHECK (valor_pago >= 0 AND valor_pago <= valor_original),
    CONSTRAINT ck_contas_pagar_status CHECK (status IN ('ABERTO', 'PAGO', 'CANCELADO'))
);

CREATE INDEX idx_contas_pagar_tenant_vencimento ON contas_pagar (tenant_id, vencimento);
CREATE INDEX idx_contas_pagar_tenant_status ON contas_pagar (tenant_id, status);
CREATE INDEX idx_contas_pagar_tenant_fornecedor ON contas_pagar (tenant_id, fornecedor_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000049', 'FINANCEIRO_PAGAR_LER', 'Consultar contas a pagar'),
    ('10000000-0000-0000-0000-000000000050', 'FINANCEIRO_PAGAR_CRIAR', 'Criar contas a pagar'),
    ('10000000-0000-0000-0000-000000000051', 'FINANCEIRO_PAGAR_BAIXAR', 'Registrar pagamento financeiro'),
    ('10000000-0000-0000-0000-000000000052', 'FINANCEIRO_PAGAR_CANCELAR', 'Cancelar contas a pagar')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN (
    'FINANCEIRO_PAGAR_LER',
    'FINANCEIRO_PAGAR_CRIAR',
    'FINANCEIRO_PAGAR_BAIXAR',
    'FINANCEIRO_PAGAR_CANCELAR'
)
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
