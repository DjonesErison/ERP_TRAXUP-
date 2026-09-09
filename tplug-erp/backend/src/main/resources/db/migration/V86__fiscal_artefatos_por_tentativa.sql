ALTER TABLE fiscal_documentos_xml
    ADD COLUMN tentativa_id UUID,
    ADD CONSTRAINT fk_fiscal_xml_tentativa FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id),
    DROP CONSTRAINT uq_fiscal_xml_documento_versao;

CREATE UNIQUE INDEX uq_fiscal_xml_original_documento_versao
    ON fiscal_documentos_xml (tenant_id, documento_id, versao)
    WHERE tentativa_id IS NULL;

CREATE UNIQUE INDEX uq_fiscal_xml_tentativa_versao
    ON fiscal_documentos_xml (tenant_id, tentativa_id, versao)
    WHERE tentativa_id IS NOT NULL;

ALTER TABLE fiscal_documentos_assinaturas
    ADD COLUMN tentativa_id UUID,
    ADD CONSTRAINT fk_fiscal_assinatura_tentativa FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id);

CREATE UNIQUE INDEX uq_fiscal_assinatura_tentativa
    ON fiscal_documentos_assinaturas (tenant_id, tentativa_id)
    WHERE tentativa_id IS NOT NULL;

ALTER TABLE fiscal_transmissoes
    ADD COLUMN tentativa_id UUID,
    ADD CONSTRAINT fk_fiscal_transmissao_tentativa FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id);

CREATE UNIQUE INDEX uq_fiscal_transmissao_tentativa
    ON fiscal_transmissoes (tenant_id, tentativa_id)
    WHERE tentativa_id IS NOT NULL;

ALTER TABLE fiscal_documentos_processados
    ADD COLUMN tentativa_id UUID,
    ADD CONSTRAINT fk_fiscal_processado_tentativa FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id);

CREATE UNIQUE INDEX uq_fiscal_processado_tentativa
    ON fiscal_documentos_processados (tenant_id, tentativa_id)
    WHERE tentativa_id IS NOT NULL;

CREATE INDEX idx_fiscal_xml_documento_tentativa
    ON fiscal_documentos_xml (tenant_id, documento_id, tentativa_id);

COMMENT ON COLUMN fiscal_documentos_xml.tentativa_id IS
    'Nulo para o artefato original; preenchido em regeneracoes apos correcao';

COMMENT ON COLUMN fiscal_documentos_assinaturas.tentativa_id IS
    'Tentativa corrigida que originou a assinatura; nulo no fluxo original';

COMMENT ON COLUMN fiscal_transmissoes.tentativa_id IS
    'Tentativa corrigida transmitida; nulo no fluxo original';

COMMENT ON COLUMN fiscal_documentos_processados.tentativa_id IS
    'Tentativa corrigida do envelope processado; nulo no fluxo original';
