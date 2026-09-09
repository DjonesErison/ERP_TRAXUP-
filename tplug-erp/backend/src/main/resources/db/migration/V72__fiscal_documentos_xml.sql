CREATE TABLE fiscal_documentos_xml (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    versao VARCHAR(10) NOT NULL,
    conteudo TEXT NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    gerado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_xml_documento UNIQUE (tenant_id, documento_id),
    CONSTRAINT fk_fiscal_xml_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT ck_fiscal_xml_versao CHECK (versao = '1.0'),
    CONSTRAINT ck_fiscal_xml_conteudo CHECK (length(conteudo) > 0),
    CONSTRAINT ck_fiscal_xml_hash CHECK (hash_sha256 ~ '^[0-9a-f]{64}$')
);

CREATE INDEX idx_fiscal_xml_tenant_gerado
    ON fiscal_documentos_xml (tenant_id, gerado_em DESC);
