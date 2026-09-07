ALTER TABLE contas_receber
    ADD CONSTRAINT fk_contas_receber_usuario_tenant
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);

ALTER TABLE contas_pagar
    ADD CONSTRAINT fk_contas_pagar_usuario_tenant
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);
