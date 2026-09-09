CREATE TABLE pdv_caixa_sessoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    terminal_id UUID NOT NULL,
    conta_financeira_id UUID NOT NULL,
    usuario_abertura_id UUID,
    usuario_fechamento_id UUID,
    status VARCHAR(16) NOT NULL,
    saldo_abertura NUMERIC(19,4) NOT NULL,
    saldo_sistema_fechamento NUMERIC(19,4),
    saldo_informado_fechamento NUMERIC(19,4),
    diferenca_fechamento NUMERIC(19,4),
    aberto_em TIMESTAMPTZ NOT NULL,
    fechado_em TIMESTAMPTZ,
    observacao_fechamento VARCHAR(500),
    CONSTRAINT ck_pdv_caixa_status CHECK (status IN ('ABERTO','FECHADO')),
    CONSTRAINT ck_pdv_caixa_saldos CHECK (saldo_abertura >= 0 AND (saldo_informado_fechamento IS NULL OR saldo_informado_fechamento >= 0)),
    CONSTRAINT ck_pdv_caixa_fechamento CHECK ((status='ABERTO' AND fechado_em IS NULL) OR (status='FECHADO' AND fechado_em IS NOT NULL AND saldo_sistema_fechamento IS NOT NULL AND saldo_informado_fechamento IS NOT NULL AND diferenca_fechamento IS NOT NULL)),
    CONSTRAINT uq_pdv_caixa_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT fk_pdv_caixa_terminal FOREIGN KEY (tenant_id, terminal_id) REFERENCES pdv_terminais (tenant_id, id),
    CONSTRAINT fk_pdv_caixa_filial FOREIGN KEY (tenant_id, filial_id) REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_pdv_caixa_conta FOREIGN KEY (tenant_id, conta_financeira_id) REFERENCES contas_financeiras (tenant_id, id)
);

CREATE UNIQUE INDEX uq_pdv_caixa_terminal_aberto
    ON pdv_caixa_sessoes (tenant_id, terminal_id)
    WHERE status = 'ABERTO';

CREATE INDEX idx_pdv_caixa_tenant_terminal_aberto_em
    ON pdv_caixa_sessoes (tenant_id, terminal_id, aberto_em DESC);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000075', 'PDV_CAIXA_LER', 'Consultar sessoes de caixa do PDV'),
    ('10000000-0000-0000-0000-000000000076', 'PDV_CAIXA_OPERAR', 'Abrir e fechar caixa do PDV')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p JOIN permissoes perm ON perm.chave IN ('PDV_CAIXA_LER','PDV_CAIXA_OPERAR')
WHERE UPPER(p.nome)='ADMIN'
ON CONFLICT DO NOTHING;
