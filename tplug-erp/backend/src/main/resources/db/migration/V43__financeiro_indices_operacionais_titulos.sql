CREATE INDEX idx_contas_receber_operacional_filial_cliente_status_vencimento
    ON contas_receber (tenant_id, filial_id, cliente_id, status, vencimento);

CREATE INDEX idx_contas_pagar_operacional_filial_fornecedor_status_vencimento
    ON contas_pagar (tenant_id, filial_id, fornecedor_id, status, vencimento);
