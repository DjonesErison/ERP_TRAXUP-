CREATE TABLE conciliacao_lancamentos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    conta_financeira_id UUID NOT NULL,
    origem VARCHAR(40) NOT NULL,
    referencia_externa VARCHAR(120) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    movimento_id UUID,
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    conciliado_em TIMESTAMPTZ,
    CONSTRAINT ck_conciliacao_tipo CHECK (tipo IN ('ENTRADA','SAIDA')),
    CONSTRAINT ck_conciliacao_valor CHECK (valor > 0),
    CONSTRAINT ck_conciliacao_status CHECK (status IN ('PENDENTE','CONCILIADO')),
    CONSTRAINT uq_conciliacao_referencia UNIQUE (tenant_id, conta_financeira_id, origem, referencia_externa),
    CONSTRAINT fk_conciliacao_conta_tenant FOREIGN KEY (tenant_id, conta_financeira_id)
        REFERENCES contas_financeiras (tenant_id, id),
    CONSTRAINT fk_conciliacao_movimento FOREIGN KEY (movimento_id)
        REFERENCES contas_financeiras_movimentos(id)
);

CREATE INDEX idx_conciliacao_tenant_conta_status
    ON conciliacao_lancamentos (tenant_id, conta_financeira_id, status, ocorrido_em DESC);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000055', 'FINANCEIRO_CONCILIACAO_LER', 'Consultar conciliacao financeira'),
    ('10000000-0000-0000-0000-000000000056', 'FINANCEIRO_CONCILIACAO_EDITAR', 'Importar e conciliar lancamentos financeiros')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('FINANCEIRO_CONCILIACAO_LER','FINANCEIRO_CONCILIACAO_EDITAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
