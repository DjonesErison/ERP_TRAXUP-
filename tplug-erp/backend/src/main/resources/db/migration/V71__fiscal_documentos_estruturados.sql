CREATE TABLE fiscal_documentos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    solicitacao_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    pedido_venda_id UUID NOT NULL,
    modelo VARCHAR(10) NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    quantidade_itens INTEGER NOT NULL,
    valor_bruto NUMERIC(19,4) NOT NULL,
    valor_desconto NUMERIC(19,4) NOT NULL,
    valor_total NUMERIC(19,4) NOT NULL,
    estruturado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_documento_solicitacao UNIQUE (tenant_id, solicitacao_id),
    CONSTRAINT uq_fiscal_documento_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT fk_fiscal_documento_solicitacao FOREIGN KEY (tenant_id, solicitacao_id)
        REFERENCES fiscal_solicitacoes (tenant_id, id),
    CONSTRAINT fk_fiscal_documento_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_fiscal_documento_venda FOREIGN KEY (tenant_id, pedido_venda_id)
        REFERENCES pedidos_venda (tenant_id, id),
    CONSTRAINT ck_fiscal_documento_modelo CHECK (modelo IN ('NFCE', 'NFE')),
    CONSTRAINT ck_fiscal_documento_ambiente CHECK (ambiente IN ('HOMOLOGACAO', 'PRODUCAO')),
    CONSTRAINT ck_fiscal_documento_status CHECK (status = 'ESTRUTURADO'),
    CONSTRAINT ck_fiscal_documento_itens CHECK (quantidade_itens > 0),
    CONSTRAINT ck_fiscal_documento_valores CHECK (
        valor_bruto >= 0 AND valor_desconto >= 0 AND valor_total >= 0
        AND valor_total = valor_bruto - valor_desconto
    )
);

CREATE INDEX idx_fiscal_documentos_tenant_status
    ON fiscal_documentos (tenant_id, status, estruturado_em DESC);
