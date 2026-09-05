CREATE TABLE contas_financeiras (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    saldo NUMERIC(19,4) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_contas_financeiras_tenant_filial
        FOREIGN KEY (tenant_id, filial_id) REFERENCES filiais (tenant_id, id),
    CONSTRAINT ck_contas_financeiras_tipo CHECK (tipo IN ('CAIXA', 'BANCO')),
    CONSTRAINT uq_contas_financeiras_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_contas_financeiras_tenant_filial_nome UNIQUE (tenant_id, filial_id, nome)
);

CREATE TABLE contas_financeiras_movimentos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    conta_financeira_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    usuario_id UUID,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_contas_financeiras_movimentos_tenant_filial
        FOREIGN KEY (tenant_id, filial_id) REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_contas_financeiras_movimentos_tenant_conta
        FOREIGN KEY (tenant_id, conta_financeira_id) REFERENCES contas_financeiras (tenant_id, id),
    CONSTRAINT ck_contas_financeiras_movimentos_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT ck_contas_financeiras_movimentos_valor CHECK (valor > 0)
);

CREATE INDEX idx_contas_financeiras_tenant_filial ON contas_financeiras (tenant_id, filial_id, ativo);
CREATE INDEX idx_contas_financeiras_movimentos_tenant_conta
    ON contas_financeiras_movimentos (tenant_id, conta_financeira_id, ocorrido_em DESC);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000053', 'FINANCEIRO_CONTA_LER', 'Consultar contas financeiras e movimentos'),
    ('10000000-0000-0000-0000-000000000054', 'FINANCEIRO_CONTA_CRIAR', 'Criar conta financeira'),
    ('10000000-0000-0000-0000-000000000055', 'FINANCEIRO_CONTA_MOVIMENTAR', 'Registrar entrada ou saida financeira'),
    ('10000000-0000-0000-0000-000000000056', 'FINANCEIRO_CONTA_DESATIVAR', 'Desativar conta financeira')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN (
    'FINANCEIRO_CONTA_LER',
    'FINANCEIRO_CONTA_CRIAR',
    'FINANCEIRO_CONTA_MOVIMENTAR',
    'FINANCEIRO_CONTA_DESATIVAR'
)
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
