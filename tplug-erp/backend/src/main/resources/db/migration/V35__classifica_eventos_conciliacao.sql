ALTER TABLE conciliacao_lancamentos
    ADD COLUMN natureza VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE conciliacao_lancamentos
    ADD CONSTRAINT ck_conciliacao_lancamentos_natureza
        CHECK (natureza IN ('NORMAL', 'TAXA', 'ANTECIPACAO', 'ESTORNO', 'CHARGEBACK'));

CREATE INDEX idx_conciliacao_lancamentos_tenant_natureza_status
    ON conciliacao_lancamentos (tenant_id, natureza, status);
