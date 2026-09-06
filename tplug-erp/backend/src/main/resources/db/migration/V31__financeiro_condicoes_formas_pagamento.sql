CREATE TABLE formas_pagamento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_formas_pagamento_tenant_codigo UNIQUE (tenant_id, codigo),
    CONSTRAINT uq_formas_pagamento_tenant_id_id UNIQUE (tenant_id, id)
);

CREATE TABLE condicoes_pagamento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_condicoes_pagamento_tenant_codigo UNIQUE (tenant_id, codigo),
    CONSTRAINT uq_condicoes_pagamento_tenant_id_id UNIQUE (tenant_id, id)
);

CREATE TABLE condicoes_pagamento_parcelas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    condicao_pagamento_id UUID NOT NULL,
    numero INTEGER NOT NULL,
    dias INTEGER NOT NULL,
    percentual NUMERIC(7,4) NOT NULL,
    CONSTRAINT ck_condicoes_pagamento_parcelas_numero CHECK (numero > 0),
    CONSTRAINT ck_condicoes_pagamento_parcelas_dias CHECK (dias >= 0),
    CONSTRAINT ck_condicoes_pagamento_parcelas_percentual CHECK (percentual > 0 AND percentual <= 100),
    CONSTRAINT uq_condicoes_pagamento_parcelas_numero UNIQUE (tenant_id, condicao_pagamento_id, numero),
    CONSTRAINT fk_condicoes_pagamento_parcelas_tenant
        FOREIGN KEY (tenant_id, condicao_pagamento_id)
        REFERENCES condicoes_pagamento (tenant_id, id)
);

CREATE INDEX idx_formas_pagamento_tenant_ativo ON formas_pagamento (tenant_id, ativo);
CREATE INDEX idx_condicoes_pagamento_tenant_ativo ON condicoes_pagamento (tenant_id, ativo);
CREATE INDEX idx_condicoes_pagamento_parcelas_condicao ON condicoes_pagamento_parcelas (tenant_id, condicao_pagamento_id, numero);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000057', 'FINANCEIRO_PAGAMENTO_CONFIG_LER', 'Consultar formas e condicoes de pagamento'),
    ('10000000-0000-0000-0000-000000000058', 'FINANCEIRO_PAGAMENTO_CONFIG_EDITAR', 'Cadastrar e alterar formas e condicoes de pagamento')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('FINANCEIRO_PAGAMENTO_CONFIG_LER', 'FINANCEIRO_PAGAMENTO_CONFIG_EDITAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
