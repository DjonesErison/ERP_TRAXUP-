ALTER TABLE integracoes_financeiras
    ADD CONSTRAINT uq_integracoes_financeiras_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE integracoes_financeiras_tentativas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    integracao_id UUID NOT NULL,
    provedor VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    quantidade_lancamentos INTEGER NOT NULL DEFAULT 0,
    duracao_ms BIGINT NOT NULL,
    erro_codigo VARCHAR(120),
    iniciado_em TIMESTAMPTZ NOT NULL,
    finalizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_integracao_tentativa_status CHECK (status IN ('SUCESSO', 'FALHA')),
    CONSTRAINT ck_integracao_tentativa_quantidade CHECK (quantidade_lancamentos >= 0),
    CONSTRAINT ck_integracao_tentativa_duracao CHECK (duracao_ms >= 0),
    CONSTRAINT fk_integracao_tentativa_integracao FOREIGN KEY (tenant_id, integracao_id)
        REFERENCES integracoes_financeiras (tenant_id, id)
);

CREATE INDEX idx_integracao_tentativa_tenant_integracao_inicio
    ON integracoes_financeiras_tentativas (tenant_id, integracao_id, iniciado_em DESC);
