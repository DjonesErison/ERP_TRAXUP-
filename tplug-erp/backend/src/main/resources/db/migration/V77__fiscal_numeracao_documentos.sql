CREATE TABLE fiscal_numeradores (
    tenant_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    modelo VARCHAR(10) NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    serie INTEGER NOT NULL,
    ultimo_numero BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_fiscal_numeradores PRIMARY KEY (
        tenant_id, filial_id, modelo, ambiente, serie
    ),
    CONSTRAINT fk_fiscal_numerador_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT ck_fiscal_numerador_modelo CHECK (modelo IN ('NFCE', 'NFE')),
    CONSTRAINT ck_fiscal_numerador_ambiente CHECK (ambiente IN ('HOMOLOGACAO', 'PRODUCAO')),
    CONSTRAINT ck_fiscal_numerador_serie CHECK (serie BETWEEN 1 AND 999),
    CONSTRAINT ck_fiscal_numerador_numero CHECK (ultimo_numero BETWEEN 0 AND 999999999)
);

ALTER TABLE fiscal_documentos
    ADD COLUMN serie INTEGER,
    ADD COLUMN numero BIGINT,
    ADD COLUMN numerado_em TIMESTAMPTZ,
    ADD CONSTRAINT ck_fiscal_documento_numeracao_completa CHECK (
        (serie IS NULL AND numero IS NULL AND numerado_em IS NULL)
        OR (
            serie BETWEEN 1 AND 999
            AND numero BETWEEN 1 AND 999999999
            AND numerado_em IS NOT NULL
        )
    ),
    ADD CONSTRAINT uq_fiscal_documento_numero UNIQUE (
        tenant_id, filial_id, modelo, ambiente, serie, numero
    );

CREATE INDEX idx_fiscal_documentos_tenant_numero
    ON fiscal_documentos (tenant_id, filial_id, modelo, ambiente, serie, numero)
    WHERE numero IS NOT NULL;
