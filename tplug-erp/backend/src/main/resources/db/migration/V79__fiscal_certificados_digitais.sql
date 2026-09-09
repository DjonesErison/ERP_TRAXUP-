CREATE TABLE fiscal_certificados_digitais (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    tipo VARCHAR(5) NOT NULL,
    titular VARCHAR(255) NOT NULL,
    documento_titular VARCHAR(14) NOT NULL,
    numero_serie VARCHAR(120) NOT NULL,
    thumbprint_sha256 CHAR(64) NOT NULL,
    validade_inicio TIMESTAMPTZ NOT NULL,
    validade_fim TIMESTAMPTZ NOT NULL,
    cofre_segredos VARCHAR(60) NOT NULL,
    referencia_segredo VARCHAR(500) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_certificado_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT fk_fiscal_certificado_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT ck_fiscal_certificado_tipo CHECK (tipo IN ('A1', 'A3')),
    CONSTRAINT ck_fiscal_certificado_documento CHECK (
        documento_titular ~ '^[0-9]{11}$' OR documento_titular ~ '^[0-9]{14}$'
    ),
    CONSTRAINT ck_fiscal_certificado_thumbprint CHECK (
        thumbprint_sha256 ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_fiscal_certificado_validade CHECK (validade_fim > validade_inicio),
    CONSTRAINT ck_fiscal_certificado_cofre CHECK (length(btrim(cofre_segredos)) > 0),
    CONSTRAINT ck_fiscal_certificado_referencia CHECK (
        length(btrim(referencia_segredo)) > 0
    )
);

CREATE UNIQUE INDEX uq_fiscal_certificado_ativo_filial
    ON fiscal_certificados_digitais (tenant_id, filial_id)
    WHERE ativo = TRUE;

CREATE INDEX idx_fiscal_certificados_validade
    ON fiscal_certificados_digitais (tenant_id, ativo, validade_fim);

COMMENT ON COLUMN fiscal_certificados_digitais.referencia_segredo IS
    'Referencia opaca ao cofre externo; nunca armazenar PFX, chave privada ou senha nesta tabela';
