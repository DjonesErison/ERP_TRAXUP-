CREATE TABLE auditorias (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    usuario_id UUID,
    empresa_id UUID,
    filial_id UUID,
    operacao VARCHAR(80) NOT NULL,
    entidade VARCHAR(120) NOT NULL,
    entidade_id UUID,
    detalhes TEXT,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditorias_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_auditorias_tenant_criado_em
    ON auditorias (tenant_id, criado_em DESC);

CREATE INDEX idx_auditorias_tenant_entidade
    ON auditorias (tenant_id, entidade, entidade_id);

CREATE INDEX idx_auditorias_tenant_usuario
    ON auditorias (tenant_id, usuario_id, criado_em DESC);
