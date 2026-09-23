CREATE TABLE trials (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    documento VARCHAR(14),
    telefone VARCHAR(30) NOT NULL,
    segmento VARCHAR(100),
    quantidade_lojas INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'ATIVO',
    inicio_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    onboarding_concluido BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trials_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_trials_empresa FOREIGN KEY (tenant_id, empresa_id) REFERENCES empresas(tenant_id, id),
    CONSTRAINT fk_trials_filial FOREIGN KEY (tenant_id, filial_id) REFERENCES filiais(tenant_id, id),
    CONSTRAINT fk_trials_usuario FOREIGN KEY (tenant_id, usuario_id) REFERENCES usuarios(tenant_id, id),
    CONSTRAINT ck_trials_quantidade_lojas CHECK (quantidade_lojas BETWEEN 1 AND 999)
);
CREATE INDEX idx_trials_tenant ON trials(tenant_id);
CREATE INDEX idx_trials_expira ON trials(expira_em);
