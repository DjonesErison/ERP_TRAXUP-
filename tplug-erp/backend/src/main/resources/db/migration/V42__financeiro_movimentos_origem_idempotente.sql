ALTER TABLE contas_financeiras_movimentos
    ADD COLUMN origem_tipo VARCHAR(40),
    ADD COLUMN origem_id UUID,
    ADD COLUMN origem_referencia VARCHAR(120);

ALTER TABLE contas_financeiras_movimentos
    ADD CONSTRAINT ck_contas_financeiras_movimentos_origem_completa
    CHECK (
        (origem_tipo IS NULL AND origem_id IS NULL AND origem_referencia IS NULL)
        OR
        (origem_tipo IS NOT NULL AND origem_id IS NOT NULL AND origem_referencia IS NOT NULL)
    );

CREATE UNIQUE INDEX uq_contas_financeiras_movimentos_origem
    ON contas_financeiras_movimentos (tenant_id, origem_tipo, origem_id, origem_referencia)
    WHERE origem_tipo IS NOT NULL;

CREATE INDEX idx_contas_financeiras_movimentos_origem
    ON contas_financeiras_movimentos (tenant_id, origem_tipo, origem_id)
    WHERE origem_tipo IS NOT NULL;
