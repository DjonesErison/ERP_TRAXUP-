ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_tenant_id
        UNIQUE (tenant_id, id);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado_em TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_refresh_tokens_tenant_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id),

    CONSTRAINT uk_refresh_tokens_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_tenant_usuario
    ON refresh_tokens (tenant_id, usuario_id);

CREATE INDEX idx_refresh_tokens_expira_em
    ON refresh_tokens (expira_em);
