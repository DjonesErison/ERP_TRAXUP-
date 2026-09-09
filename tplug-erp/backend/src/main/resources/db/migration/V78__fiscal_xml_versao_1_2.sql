ALTER TABLE fiscal_documentos_xml
    DROP CONSTRAINT ck_fiscal_xml_versao,
    ADD CONSTRAINT ck_fiscal_xml_versao CHECK (versao IN ('1.0', '1.1', '1.2'));
