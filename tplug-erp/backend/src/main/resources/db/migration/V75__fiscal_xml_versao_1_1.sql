ALTER TABLE fiscal_documentos_xml
    DROP CONSTRAINT uq_fiscal_xml_documento,
    DROP CONSTRAINT ck_fiscal_xml_versao,
    ADD CONSTRAINT uq_fiscal_xml_documento_versao UNIQUE (tenant_id, documento_id, versao),
    ADD CONSTRAINT ck_fiscal_xml_versao CHECK (versao IN ('1.0', '1.1'));
