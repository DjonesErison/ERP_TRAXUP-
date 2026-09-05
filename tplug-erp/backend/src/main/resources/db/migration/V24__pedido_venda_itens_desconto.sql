ALTER TABLE pedido_venda_itens
    ADD COLUMN desconto_valor NUMERIC(19,4) NOT NULL DEFAULT 0;

ALTER TABLE pedido_venda_itens
    ADD CONSTRAINT ck_pedido_venda_itens_desconto_nao_negativo CHECK (desconto_valor >= 0),
    ADD CONSTRAINT ck_pedido_venda_itens_desconto_limite CHECK (desconto_valor <= quantidade * preco_unitario);
