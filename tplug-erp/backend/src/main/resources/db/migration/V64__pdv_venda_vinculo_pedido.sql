ALTER TABLE pedidos_venda
    ADD CONSTRAINT uq_pedidos_venda_tenant_id UNIQUE (tenant_id, id);

ALTER TABLE pdv_vendas_sincronizacao
    ADD COLUMN pedido_venda_id UUID,
    ADD COLUMN pedido_venda_vinculado_em TIMESTAMPTZ,
    ADD CONSTRAINT ck_pdv_sync_pedido_vinculo_consistente CHECK (
        (pedido_venda_id IS NULL AND pedido_venda_vinculado_em IS NULL)
        OR (pedido_venda_id IS NOT NULL AND pedido_venda_vinculado_em IS NOT NULL)
    ),
    ADD CONSTRAINT uq_pdv_sync_tenant_pedido UNIQUE (tenant_id, pedido_venda_id),
    ADD CONSTRAINT fk_pdv_sync_tenant_pedido FOREIGN KEY (tenant_id, pedido_venda_id)
        REFERENCES pedidos_venda (tenant_id, id);

CREATE INDEX idx_pdv_sync_tenant_pedido
    ON pdv_vendas_sincronizacao (tenant_id, pedido_venda_id)
    WHERE pedido_venda_id IS NOT NULL;
