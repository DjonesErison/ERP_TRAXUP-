ALTER TABLE fiscal_regras_operacao
    ADD CONSTRAINT uq_fiscal_regras_tenant_id UNIQUE (tenant_id, id);

ALTER TABLE fiscal_documentos
    ADD COLUMN regra_operacao_id UUID,
    ADD COLUMN tipo_operacao VARCHAR(20),
    ADD COLUMN regime_tributario VARCHAR(20),
    ADD COLUMN uf_destino CHAR(2),
    ADD COLUMN cfop CHAR(4),
    ADD COLUMN cst_icms VARCHAR(2),
    ADD COLUMN csosn VARCHAR(3),
    ADD COLUMN regra_aplicada_em TIMESTAMPTZ,
    ADD CONSTRAINT fk_fiscal_documento_regra FOREIGN KEY (tenant_id, regra_operacao_id)
        REFERENCES fiscal_regras_operacao (tenant_id, id),
    ADD CONSTRAINT ck_fiscal_documento_regra_consistente CHECK (
        (regra_operacao_id IS NULL AND tipo_operacao IS NULL AND regime_tributario IS NULL
            AND uf_destino IS NULL AND cfop IS NULL AND cst_icms IS NULL
            AND csosn IS NULL AND regra_aplicada_em IS NULL)
        OR
        (regra_operacao_id IS NOT NULL AND tipo_operacao IS NOT NULL
            AND regime_tributario IS NOT NULL AND uf_destino IS NOT NULL
            AND cfop IS NOT NULL AND regra_aplicada_em IS NOT NULL
            AND (
                (regime_tributario = 'SIMPLES_NACIONAL' AND csosn IS NOT NULL AND cst_icms IS NULL)
                OR
                (regime_tributario = 'REGIME_NORMAL' AND cst_icms IS NOT NULL AND csosn IS NULL)
            ))
    );

CREATE INDEX idx_fiscal_documentos_regra
    ON fiscal_documentos (tenant_id, regra_operacao_id)
    WHERE regra_operacao_id IS NOT NULL;
