ALTER TABLE contas_financeiras_movimentos
    ADD CONSTRAINT uq_contas_financeiras_movimentos_tenant_filial_conta_id
        UNIQUE (tenant_id, filial_id, conta_financeira_id, id);

ALTER TABLE conciliacao_lancamentos
    DROP CONSTRAINT fk_conciliacao_movimento_tenant,
    ADD CONSTRAINT fk_conciliacao_movimento_contexto
        FOREIGN KEY (tenant_id, filial_id, conta_financeira_id, movimento_id)
        REFERENCES contas_financeiras_movimentos (tenant_id, filial_id, conta_financeira_id, id),
    ADD CONSTRAINT ck_conciliacao_estado_coerente CHECK (
        (status = 'PENDENTE' AND movimento_id IS NULL AND conciliado_em IS NULL)
        OR
        (status = 'CONCILIADO' AND movimento_id IS NOT NULL AND conciliado_em IS NOT NULL)
    );

CREATE UNIQUE INDEX uq_conciliacao_movimento
    ON conciliacao_lancamentos (tenant_id, movimento_id)
    WHERE movimento_id IS NOT NULL;
