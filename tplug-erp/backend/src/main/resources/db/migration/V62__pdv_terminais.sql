CREATE TABLE pdv_terminais (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    codigo VARCHAR(64) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    serie INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pdv_terminais_serie CHECK (serie > 0),
    CONSTRAINT uq_pdv_terminais_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_pdv_terminais_tenant_codigo UNIQUE (tenant_id, codigo),
    CONSTRAINT uq_pdv_terminais_tenant_filial_serie UNIQUE (tenant_id, filial_id, serie),
    CONSTRAINT fk_pdv_terminal_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id)
);

CREATE INDEX idx_pdv_terminais_tenant_filial_ativo
    ON pdv_terminais (tenant_id, filial_id, ativo, nome);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000066', 'PDV_TERMINAL_LER', 'Consultar terminais do PDV'),
    ('10000000-0000-0000-0000-000000000067', 'PDV_TERMINAL_GERENCIAR', 'Criar, ativar e desativar terminais do PDV')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('PDV_TERMINAL_LER', 'PDV_TERMINAL_GERENCIAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
