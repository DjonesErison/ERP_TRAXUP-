ALTER TABLE pedidos_venda
    ADD CONSTRAINT uq_pedidos_venda_tenant_id UNIQUE (tenant_id, id);

ALTER TABLE pdv_vendas_sincronizacao
    ADD COLUMN pedido_venda_id UUID,
    ADD COLUMN processado_em TIMESTAMPTZ,
    ADD CONSTRAINT ck_pdv_vendas_sync_processamento
        CHECK ((pedido_venda_id IS NULL AND processado_em IS NULL)
            OR (pedido_venda_id IS NOT NULL AND processado_em IS NOT NULL)),
    ADD CONSTRAINT fk_pdv_vendas_sync_tenant_pedido
        FOREIGN KEY (tenant_id, pedido_venda_id)
        REFERENCES pedidos_venda (tenant_id, id),
    ADD CONSTRAINT uq_pdv_vendas_sync_tenant_pedido UNIQUE (tenant_id, pedido_venda_id);

CREATE INDEX idx_pdv_vendas_sync_tenant_processado
    ON pdv_vendas_sincronizacao (tenant_id, processado_em DESC)
    WHERE processado_em IS NOT NULL;
