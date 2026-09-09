CREATE TABLE fiscal_regras_operacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    tipo_operacao VARCHAR(20) NOT NULL,
    modelo VARCHAR(10) NOT NULL,
    regime_tributario VARCHAR(20) NOT NULL,
    uf_destino CHAR(2) NOT NULL,
    cfop CHAR(4) NOT NULL,
    cst_icms VARCHAR(3),
    csosn VARCHAR(3),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_regra_contexto UNIQUE
        (tenant_id, tipo_operacao, modelo, regime_tributario, uf_destino),
    CONSTRAINT ck_fiscal_regra_operacao CHECK (tipo_operacao IN ('VENDA', 'DEVOLUCAO')),
    CONSTRAINT ck_fiscal_regra_modelo CHECK (modelo IN ('NFCE', 'NFE')),
    CONSTRAINT ck_fiscal_regra_regime CHECK (regime_tributario IN ('SIMPLES_NACIONAL', 'REGIME_NORMAL')),
    CONSTRAINT ck_fiscal_regra_uf CHECK (uf_destino ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_fiscal_regra_cfop CHECK (cfop ~ '^[0-9]{4}$'),
    CONSTRAINT ck_fiscal_regra_icms CHECK (
        (regime_tributario = 'SIMPLES_NACIONAL' AND csosn ~ '^[0-9]{3}$' AND cst_icms IS NULL)
        OR (regime_tributario = 'REGIME_NORMAL' AND cst_icms ~ '^[0-9]{2,3}$' AND csosn IS NULL)
    )
);

CREATE INDEX idx_fiscal_regras_resolucao
    ON fiscal_regras_operacao
    (tenant_id, tipo_operacao, modelo, regime_tributario, uf_destino)
    WHERE ativo;
