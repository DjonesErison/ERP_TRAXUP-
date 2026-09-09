ALTER TABLE pedido_venda_itens
    ADD CONSTRAINT uq_pedido_venda_itens_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE fiscal_solicitacao_itens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    solicitacao_id UUID NOT NULL,
    pedido_venda_item_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    grade_id UUID,
    codigo VARCHAR(60) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    ncm VARCHAR(8),
    unidade VARCHAR(10) NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    preco_unitario NUMERIC(19,4) NOT NULL,
    adicional_combo_unitario NUMERIC(19,4) NOT NULL,
    desconto_valor NUMERIC(19,4) NOT NULL,
    total_item NUMERIC(19,4) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_item_snapshot UNIQUE (tenant_id, solicitacao_id, pedido_venda_item_id),
    CONSTRAINT fk_fiscal_snapshot_solicitacao FOREIGN KEY (tenant_id, solicitacao_id)
        REFERENCES fiscal_solicitacoes (tenant_id, id),
    CONSTRAINT fk_fiscal_snapshot_item FOREIGN KEY (tenant_id, pedido_venda_item_id)
        REFERENCES pedido_venda_itens (tenant_id, id),
    CONSTRAINT ck_fiscal_snapshot_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_fiscal_snapshot_valores CHECK (
        preco_unitario >= 0 AND adicional_combo_unitario >= 0
        AND desconto_valor >= 0 AND total_item >= 0
    )
);

CREATE INDEX idx_fiscal_item_snapshot_solicitacao
    ON fiscal_solicitacao_itens (tenant_id, solicitacao_id);
