ALTER TABLE conciliacao_lancamentos
    ADD CONSTRAINT fk_conciliacao_usuario_tenant
    FOREIGN KEY (tenant_id, usuario_id)
    REFERENCES usuarios (tenant_id, id);
