ALTER TABLE pedido_venda_itens
    ADD COLUMN pdv_sincronizacao_id UUID,
    ADD COLUMN pdv_item_local_id UUID;

ALTER TABLE pedido_venda_itens
    ADD CONSTRAINT fk_pedido_venda_itens_pdv_sync
        FOREIGN KEY (tenant_id, pdv_sincronizacao_id)
        REFERENCES pdv_vendas_sincronizacao (tenant_id, id),
    ADD CONSTRAINT ck_pedido_venda_itens_pdv_identidade
        CHECK ((pdv_sincronizacao_id IS NULL AND pdv_item_local_id IS NULL)
            OR (pdv_sincronizacao_id IS NOT NULL AND pdv_item_local_id IS NOT NULL)),
    ADD CONSTRAINT uq_pedido_venda_itens_pdv_local
        UNIQUE (tenant_id, pdv_sincronizacao_id, pdv_item_local_id);

CREATE INDEX idx_pedido_venda_itens_pdv_sync
    ON pedido_venda_itens (tenant_id, pdv_sincronizacao_id);
