ALTER TABLE contas_financeiras
    ADD CONSTRAINT uq_contas_financeiras_tenant_filial_id
        UNIQUE (tenant_id, filial_id, id);

ALTER TABLE integracoes_financeiras
    DROP CONSTRAINT fk_integracao_financeira_conta;

ALTER TABLE integracoes_financeiras
    ADD CONSTRAINT fk_integracao_financeira_conta_tenant_filial
        FOREIGN KEY (tenant_id, filial_id, conta_financeira_id)
        REFERENCES contas_financeiras (tenant_id, filial_id, id);

ALTER TABLE integracoes_financeiras
    ADD CONSTRAINT fk_integracao_financeira_usuario_tenant
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);
