CREATE TABLE integracoes_financeiras (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    conta_financeira_id UUID NOT NULL,
    provedor VARCHAR(40) NOT NULL,
    identificador_externo VARCHAR(120),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_integracao_financeira_tenant_conta_provedor
        UNIQUE (tenant_id, conta_financeira_id, provedor),
    CONSTRAINT fk_integracao_financeira_conta
        FOREIGN KEY (tenant_id, conta_financeira_id)
        REFERENCES contas_financeiras (tenant_id, id),
    CONSTRAINT fk_integracao_financeira_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id)
);

CREATE INDEX idx_integracoes_financeiras_tenant_conta
    ON integracoes_financeiras (tenant_id, conta_financeira_id);

CREATE INDEX idx_integracoes_financeiras_tenant_provedor_ativo
    ON integracoes_financeiras (tenant_id, provedor, ativo);
