ALTER TABLE empresas
    ADD CONSTRAINT uk_empresas_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE filiais
    ADD CONSTRAINT fk_filiais_tenant_empresa
    FOREIGN KEY (tenant_id, empresa_id)
    REFERENCES empresas (tenant_id, id);

CREATE INDEX idx_filiais_tenant_empresa
    ON filiais (tenant_id, empresa_id);
