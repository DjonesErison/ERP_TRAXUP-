CREATE TABLE fiscal_documentos_processados (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    solicitacao_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    assinatura_id UUID NOT NULL,
    transmissao_id UUID NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    versao VARCHAR(10) NOT NULL,
    conteudo TEXT NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    gerado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_processado_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_processado_transmissao UNIQUE (tenant_id, transmissao_id),
    CONSTRAINT fk_fiscal_processado_solicitacao FOREIGN KEY (tenant_id, solicitacao_id)
        REFERENCES fiscal_solicitacoes (tenant_id, id),
    CONSTRAINT fk_fiscal_processado_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT fk_fiscal_processado_assinatura FOREIGN KEY (tenant_id, assinatura_id)
        REFERENCES fiscal_documentos_assinaturas (tenant_id, id),
    CONSTRAINT fk_fiscal_processado_transmissao FOREIGN KEY (tenant_id, transmissao_id)
        REFERENCES fiscal_transmissoes (tenant_id, id),
    CONSTRAINT ck_fiscal_processado_ambiente CHECK (ambiente = 'HOMOLOGACAO'),
    CONSTRAINT ck_fiscal_processado_tipo CHECK (tipo = 'PROCESSADO_SIMULADO'),
    CONSTRAINT ck_fiscal_processado_versao CHECK (versao = '1.0'),
    CONSTRAINT ck_fiscal_processado_conteudo CHECK (length(btrim(conteudo)) > 0),
    CONSTRAINT ck_fiscal_processado_hash CHECK (hash_sha256 ~ '^[0-9a-f]{64}$')
);

CREATE INDEX idx_fiscal_processados_tenant_data
    ON fiscal_documentos_processados (tenant_id, gerado_em DESC);

COMMENT ON TABLE fiscal_documentos_processados IS
    'XML processado de homologacao; V82 aceita somente artefato simulado sem validade fiscal';

COMMENT ON COLUMN fiscal_documentos_processados.conteudo IS
    'Envelope TraxUP com XML assinado simulado e protocolo SIM; nao e procNFe oficial';
