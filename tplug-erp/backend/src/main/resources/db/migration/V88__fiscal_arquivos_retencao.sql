CREATE TABLE fiscal_arquivos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    tentativa_id UUID,
    processado_id UUID NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    chave_objeto VARCHAR(500) NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    tentativas_envio INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    arquivado_em TIMESTAMPTZ,
    retencao_ate DATE NOT NULL,
    CONSTRAINT uq_fiscal_arquivo_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_arquivo_processado UNIQUE (tenant_id, processado_id),
    CONSTRAINT uq_fiscal_arquivo_chave UNIQUE (tenant_id, chave_objeto),
    CONSTRAINT fk_fiscal_arquivo_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT fk_fiscal_arquivo_tentativa FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id),
    CONSTRAINT fk_fiscal_arquivo_processado FOREIGN KEY (tenant_id, processado_id)
        REFERENCES fiscal_documentos_processados (tenant_id, id),
    CONSTRAINT ck_fiscal_arquivo_tipo CHECK (
        tipo = 'XML_PROCESSADO_SIMULADO'
    ),
    CONSTRAINT ck_fiscal_arquivo_status CHECK (
        status IN ('PENDENTE', 'ARQUIVANDO', 'ARQUIVADO', 'FALHOU')
    ),
    CONSTRAINT ck_fiscal_arquivo_tentativas CHECK (tentativas_envio >= 0),
    CONSTRAINT ck_fiscal_arquivo_hash CHECK (hash_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_fiscal_arquivo_chave CHECK (length(btrim(chave_objeto)) > 0),
    CONSTRAINT ck_fiscal_arquivo_datas CHECK (
        (status = 'ARQUIVADO' AND arquivado_em IS NOT NULL)
        OR (status <> 'ARQUIVADO' AND arquivado_em IS NULL)
    ),
    CONSTRAINT ck_fiscal_arquivo_retencao CHECK (
        retencao_ate >= criado_em::date + INTERVAL '5 years'
    )
);

CREATE INDEX idx_fiscal_arquivos_pendentes
    ON fiscal_arquivos (tenant_id, criado_em)
    WHERE status IN ('PENDENTE', 'FALHOU');

CREATE INDEX idx_fiscal_arquivos_retencao
    ON fiscal_arquivos (tenant_id, retencao_ate);

COMMENT ON TABLE fiscal_arquivos IS
    'Fila e metadados do repositorio fiscal com retencao minima de cinco anos';

COMMENT ON COLUMN fiscal_arquivos.chave_objeto IS
    'Chave logica no armazenamento S3 compativel; nunca contem credenciais';

COMMENT ON COLUMN fiscal_arquivos.hash_sha256 IS
    'Hash do XML processado para verificacao de integridade apos arquivamento';
