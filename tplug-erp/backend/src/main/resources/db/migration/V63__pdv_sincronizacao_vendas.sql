CREATE TABLE pdv_vendas_sincronizacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    terminal_id UUID NOT NULL,
    operacao_local_id UUID NOT NULL,
    serie INTEGER NOT NULL,
    numero_local BIGINT NOT NULL,
    checksum CHAR(64) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    recebido_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pdv_vendas_sync_serie CHECK (serie > 0),
    CONSTRAINT ck_pdv_vendas_sync_numero CHECK (numero_local > 0),
    CONSTRAINT ck_pdv_vendas_sync_checksum CHECK (checksum ~ '^[0-9a-f]{64}$'),
    CONSTRAINT uq_pdv_vendas_sync_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_pdv_vendas_sync_operacao UNIQUE (tenant_id, terminal_id, operacao_local_id),
    CONSTRAINT uq_pdv_vendas_sync_numero UNIQUE (tenant_id, filial_id, serie, numero_local),
    CONSTRAINT fk_pdv_vendas_sync_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_pdv_vendas_sync_tenant_terminal FOREIGN KEY (tenant_id, terminal_id)
        REFERENCES pdv_terminais (tenant_id, id)
);

CREATE INDEX idx_pdv_vendas_sync_tenant_terminal_recebido
    ON pdv_vendas_sincronizacao (tenant_id, terminal_id, recebido_em DESC);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000068', 'PDV_SINCRONIZAR', 'Sincronizar operacoes locais do PDV com a nuvem')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave = 'PDV_SINCRONIZAR'
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
