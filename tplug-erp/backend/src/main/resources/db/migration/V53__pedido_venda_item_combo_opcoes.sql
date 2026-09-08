ALTER TABLE pedido_venda_itens
    ADD CONSTRAINT uk_pedido_venda_itens_id_tenant UNIQUE (id, tenant_id);

CREATE TABLE pedido_venda_item_combo_opcoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    pedido_venda_item_id UUID NOT NULL,
    grupo_id UUID NOT NULL,
    opcao_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_pedido_venda_item_combo_opcao UNIQUE (tenant_id, pedido_venda_item_id, grupo_id, opcao_id),
    CONSTRAINT fk_pedido_venda_item_combo_item_tenant
        FOREIGN KEY (pedido_venda_item_id, tenant_id) REFERENCES pedido_venda_itens(id, tenant_id),
    CONSTRAINT fk_pedido_venda_item_combo_grupo_tenant
        FOREIGN KEY (grupo_id, tenant_id) REFERENCES produto_combo_grupos(id, tenant_id),
    CONSTRAINT fk_pedido_venda_item_combo_opcao
        FOREIGN KEY (opcao_id) REFERENCES produto_combo_grupo_opcoes(id)
);

CREATE INDEX idx_pedido_venda_item_combo_tenant_item
    ON pedido_venda_item_combo_opcoes (tenant_id, pedido_venda_item_id);
