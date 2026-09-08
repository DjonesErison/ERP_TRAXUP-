ALTER TABLE pedido_venda_itens
    ADD COLUMN adicional_combo_unitario NUMERIC(19,4) NOT NULL DEFAULT 0;

ALTER TABLE pedido_venda_item_combo_opcoes
    ADD COLUMN produto_id UUID,
    ADD COLUMN quantidade NUMERIC(19,4),
    ADD COLUMN valor_adicional NUMERIC(19,4);

UPDATE pedido_venda_item_combo_opcoes selecao
SET produto_id = opcao.produto_id,
    quantidade = opcao.quantidade,
    valor_adicional = opcao.valor_adicional
FROM produto_combo_grupo_opcoes opcao
WHERE opcao.id = selecao.opcao_id
  AND opcao.tenant_id = selecao.tenant_id
  AND opcao.grupo_id = selecao.grupo_id;

ALTER TABLE pedido_venda_item_combo_opcoes
    ALTER COLUMN produto_id SET NOT NULL,
    ALTER COLUMN quantidade SET NOT NULL,
    ALTER COLUMN valor_adicional SET NOT NULL,
    ADD CONSTRAINT ck_pedido_venda_combo_opcao_quantidade CHECK (quantidade > 0),
    ADD CONSTRAINT ck_pedido_venda_combo_opcao_adicional CHECK (valor_adicional >= 0),
    ADD CONSTRAINT fk_pedido_venda_combo_opcao_produto_tenant
        FOREIGN KEY (produto_id, tenant_id) REFERENCES produtos(id, tenant_id);

UPDATE pedido_venda_itens item
SET adicional_combo_unitario = COALESCE((
    SELECT SUM(selecao.valor_adicional)
    FROM pedido_venda_item_combo_opcoes selecao
    WHERE selecao.tenant_id = item.tenant_id
      AND selecao.pedido_venda_item_id = item.id
), 0),
    total_item = quantidade * (preco_unitario + COALESCE((
        SELECT SUM(selecao.valor_adicional)
        FROM pedido_venda_item_combo_opcoes selecao
        WHERE selecao.tenant_id = item.tenant_id
          AND selecao.pedido_venda_item_id = item.id
    ), 0)) - desconto_valor;

CREATE INDEX idx_pedido_venda_combo_opcao_tenant_produto
    ON pedido_venda_item_combo_opcoes (tenant_id, produto_id);
