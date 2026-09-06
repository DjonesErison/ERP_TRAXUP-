ALTER TABLE contas_receber
    ADD COLUMN origem_tipo VARCHAR(40),
    ADD COLUMN origem_id UUID,
    ADD COLUMN origem_referencia VARCHAR(80);

ALTER TABLE contas_receber
    ADD CONSTRAINT ck_contas_receber_origem_completa
        CHECK (
            (origem_tipo IS NULL AND origem_id IS NULL AND origem_referencia IS NULL)
            OR
            (origem_tipo IS NOT NULL AND origem_id IS NOT NULL AND origem_referencia IS NOT NULL)
        );

CREATE UNIQUE INDEX uq_contas_receber_origem
    ON contas_receber (tenant_id, origem_tipo, origem_id, origem_referencia)
    WHERE origem_tipo IS NOT NULL;

CREATE INDEX idx_contas_receber_tenant_origem
    ON contas_receber (tenant_id, origem_tipo, origem_id)
    WHERE origem_tipo IS NOT NULL;
