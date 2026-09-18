CREATE TABLE ativacao_admin_tokens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expira_em TIMESTAMPTZ NOT NULL,
    usado_em TIMESTAMPTZ NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ativacao_admin_usuario ON ativacao_admin_tokens (tenant_id, usuario_id, criado_em DESC);
CREATE INDEX idx_ativacao_admin_expiracao ON ativacao_admin_tokens (expira_em) WHERE usado_em IS NULL;
