ALTER TABLE integracoes_financeiras
    ADD COLUMN checkpoint VARCHAR(500),
    ADD COLUMN sincronizado_em TIMESTAMPTZ,
    ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_integracoes_financeiras_sincronizado_em
    ON integracoes_financeiras (tenant_id, sincronizado_em);
