ALTER TABLE fiscal_documentos_xml
    ADD CONSTRAINT uq_fiscal_xml_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE fiscal_documentos_assinaturas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    xml_id UUID NOT NULL,
    certificado_id UUID NOT NULL,
    versao_xml VARCHAR(10) NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    tipo_assinatura VARCHAR(20) NOT NULL,
    algoritmo VARCHAR(40) NOT NULL,
    conteudo_assinado TEXT NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    assinado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_assinatura_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_assinatura_xml UNIQUE (tenant_id, xml_id),
    CONSTRAINT fk_fiscal_assinatura_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT fk_fiscal_assinatura_xml FOREIGN KEY (tenant_id, xml_id)
        REFERENCES fiscal_documentos_xml (tenant_id, id),
    CONSTRAINT fk_fiscal_assinatura_certificado FOREIGN KEY (tenant_id, certificado_id)
        REFERENCES fiscal_certificados_digitais (tenant_id, id),
    CONSTRAINT ck_fiscal_assinatura_versao CHECK (versao_xml = '1.2'),
    CONSTRAINT ck_fiscal_assinatura_ambiente CHECK (ambiente = 'HOMOLOGACAO'),
    CONSTRAINT ck_fiscal_assinatura_tipo CHECK (tipo_assinatura = 'SIMULADA'),
    CONSTRAINT ck_fiscal_assinatura_algoritmo CHECK (algoritmo = 'SIMULADO_SHA256'),
    CONSTRAINT ck_fiscal_assinatura_conteudo CHECK (length(btrim(conteudo_assinado)) > 0),
    CONSTRAINT ck_fiscal_assinatura_hash CHECK (hash_sha256 ~ '^[0-9a-f]{64}$')
);

CREATE INDEX idx_fiscal_assinaturas_tenant_data
    ON fiscal_documentos_assinaturas (tenant_id, assinado_em DESC);

COMMENT ON TABLE fiscal_documentos_assinaturas IS
    'Artefatos de assinatura fiscal; V80 aceita somente simulacao em homologacao';

COMMENT ON COLUMN fiscal_documentos_assinaturas.conteudo_assinado IS
    'XML de homologacao produzido pelo adaptador simulado; nao possui validade fiscal';

COMMENT ON COLUMN fiscal_documentos_assinaturas.certificado_id IS
    'Metadado do certificado utilizado; nenhum PFX, senha ou chave privada e armazenado';
