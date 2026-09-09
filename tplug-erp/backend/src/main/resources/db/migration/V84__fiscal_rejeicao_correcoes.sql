CREATE TABLE fiscal_rejeicao_correcoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    rejeicao_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    versao INTEGER NOT NULL,
    campos_corrigidos JSONB NOT NULL,
    valores_corrigidos JSONB NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    criado_por UUID,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aplicada_em TIMESTAMPTZ,
    CONSTRAINT uq_fiscal_correcao_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_correcao_versao UNIQUE (tenant_id, rejeicao_id, versao),
    CONSTRAINT fk_fiscal_correcao_rejeicao FOREIGN KEY (tenant_id, rejeicao_id)
        REFERENCES fiscal_rejeicoes (tenant_id, id),
    CONSTRAINT fk_fiscal_correcao_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT ck_fiscal_correcao_versao CHECK (versao > 0),
    CONSTRAINT ck_fiscal_correcao_campos_array CHECK (
        jsonb_typeof(campos_corrigidos) = 'array'
        AND jsonb_array_length(campos_corrigidos) > 0
    ),
    CONSTRAINT ck_fiscal_correcao_valores_objeto CHECK (
        jsonb_typeof(valores_corrigidos) = 'object'
        AND valores_corrigidos <> '{}'::jsonb
    ),
    CONSTRAINT ck_fiscal_correcao_motivo CHECK (length(btrim(motivo)) > 0),
    CONSTRAINT ck_fiscal_correcao_hash CHECK (hash_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_fiscal_correcao_status CHECK (
        status IN ('REGISTRADA', 'APLICADA', 'DESCARTADA')
    ),
    CONSTRAINT ck_fiscal_correcao_aplicacao CHECK (
        (status = 'REGISTRADA' AND aplicada_em IS NULL)
        OR (status = 'APLICADA' AND aplicada_em IS NOT NULL)
        OR (status = 'DESCARTADA')
    )
);

CREATE INDEX idx_fiscal_correcoes_rejeicao_versao
    ON fiscal_rejeicao_correcoes (tenant_id, rejeicao_id, versao DESC);

CREATE UNIQUE INDEX uq_fiscal_correcao_aplicada
    ON fiscal_rejeicao_correcoes (tenant_id, rejeicao_id)
    WHERE status = 'APLICADA';

COMMENT ON TABLE fiscal_rejeicao_correcoes IS
    'Revisoes imutaveis dos valores propostos para corrigir rejeicoes fiscais';

COMMENT ON COLUMN fiscal_rejeicao_correcoes.valores_corrigidos IS
    'Somente campos fiscais autorizados; proibido armazenar senhas, tokens, PFX ou chaves privadas';

COMMENT ON COLUMN fiscal_rejeicao_correcoes.hash_sha256 IS
    'Hash canonico de rejeicao, versao, campos, valores e motivo para verificacao de integridade';

COMMENT ON COLUMN fiscal_rejeicao_correcoes.criado_por IS
    'Usuario responsavel; nulo apenas para correcao gerada automaticamente pelo sistema';
