CREATE TABLE fiscal_perfis_filial (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    regime_tributario VARCHAR(20) NOT NULL,
    crt SMALLINT NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    serie_nfe INTEGER NOT NULL,
    serie_nfce INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_perfil_filial UNIQUE (tenant_id, filial_id),
    CONSTRAINT uq_fiscal_perfil_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT fk_fiscal_perfil_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT ck_fiscal_perfil_regime CHECK (
        regime_tributario IN ('SIMPLES_NACIONAL', 'REGIME_NORMAL')
    ),
    CONSTRAINT ck_fiscal_perfil_crt CHECK (
        (regime_tributario = 'SIMPLES_NACIONAL' AND crt IN (1, 2))
        OR (regime_tributario = 'REGIME_NORMAL' AND crt = 3)
    ),
    CONSTRAINT ck_fiscal_perfil_ambiente CHECK (ambiente IN ('HOMOLOGACAO', 'PRODUCAO')),
    CONSTRAINT ck_fiscal_perfil_series CHECK (
        serie_nfe BETWEEN 1 AND 999 AND serie_nfce BETWEEN 1 AND 999
    )
);

CREATE INDEX idx_fiscal_perfis_tenant_ativo
    ON fiscal_perfis_filial (tenant_id, ativo);
