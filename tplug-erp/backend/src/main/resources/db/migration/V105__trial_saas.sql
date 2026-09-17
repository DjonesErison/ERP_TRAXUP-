CREATE TABLE trials_saas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    administrador_id UUID NOT NULL REFERENCES usuarios(id),
    email VARCHAR(254) NOT NULL,
    documento VARCHAR(14) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    segmento VARCHAR(80),
    quantidade_lojas INTEGER NOT NULL DEFAULT 1 CHECK (quantidade_lojas > 0),
    inicio_em TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ATIVO','EXPIRADO','CONVERTIDO','CANCELADO')),
    termos_versao VARCHAR(40) NOT NULL,
    termos_aceitos_em TIMESTAMPTZ NOT NULL,
    onboarding_status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (onboarding_status IN ('PENDENTE','EM_ANDAMENTO','CONCLUIDO')),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_trial_periodo CHECK (expira_em > inicio_em)
);

CREATE UNIQUE INDEX uq_trial_tenant ON trials_saas (tenant_id);
CREATE INDEX idx_trial_status_expiracao ON trials_saas (status, expira_em);
CREATE INDEX idx_trial_email ON trials_saas (LOWER(email));
CREATE INDEX idx_trial_documento ON trials_saas (documento);
