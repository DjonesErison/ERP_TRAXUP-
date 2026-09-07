ALTER TABLE conciliacao_lancamentos
    DROP CONSTRAINT fk_conciliacao_conta_tenant;

ALTER TABLE conciliacao_lancamentos
    ADD CONSTRAINT fk_conciliacao_conta_tenant_filial
        FOREIGN KEY (tenant_id, filial_id, conta_financeira_id)
        REFERENCES contas_financeiras (tenant_id, filial_id, id);
