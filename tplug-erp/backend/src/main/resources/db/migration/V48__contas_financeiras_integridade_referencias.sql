ALTER TABLE contas_financeiras
    ADD CONSTRAINT fk_contas_financeiras_usuario_tenant
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);

ALTER TABLE contas_financeiras_movimentos
    DROP CONSTRAINT fk_contas_financeiras_movimentos_tenant_conta;

ALTER TABLE contas_financeiras_movimentos
    ADD CONSTRAINT fk_contas_financeiras_movimentos_tenant_filial_conta
        FOREIGN KEY (tenant_id, filial_id, conta_financeira_id)
        REFERENCES contas_financeiras (tenant_id, filial_id, id);

ALTER TABLE contas_financeiras_movimentos
    ADD CONSTRAINT fk_contas_financeiras_movimentos_usuario_tenant
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);
