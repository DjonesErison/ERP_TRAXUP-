ALTER TABLE contas_pagar DROP CONSTRAINT ck_contas_pagar_status;
ALTER TABLE contas_pagar
    ADD CONSTRAINT ck_contas_pagar_status
    CHECK (status IN ('ABERTO', 'PARCIAL', 'PAGO', 'CANCELADO'));

ALTER TABLE contas_pagar
    ADD CONSTRAINT uq_contas_pagar_tenant_id_id UNIQUE (tenant_id, id);

ALTER TABLE contas_pagar
    DROP CONSTRAINT contas_pagar_filial_id_fkey,
    DROP CONSTRAINT contas_pagar_fornecedor_id_fkey;

ALTER TABLE contas_pagar
    ADD CONSTRAINT fk_contas_pagar_tenant_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    ADD CONSTRAINT fk_contas_pagar_tenant_fornecedor
        FOREIGN KEY (tenant_id, fornecedor_id)
        REFERENCES pessoas (tenant_id, id);

CREATE TABLE contas_pagar_pagamentos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    conta_pagar_id UUID NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    usuario_id UUID,
    pago_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_contas_pagar_pagamentos_valor CHECK (valor > 0),
    CONSTRAINT fk_contas_pagar_pagamentos_conta_tenant
        FOREIGN KEY (tenant_id, conta_pagar_id)
        REFERENCES contas_pagar (tenant_id, id),
    CONSTRAINT fk_contas_pagar_pagamentos_tenant_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id)
);

CREATE INDEX idx_contas_pagar_pagamentos_tenant_conta
    ON contas_pagar_pagamentos (tenant_id, conta_pagar_id, pago_em DESC);

INSERT INTO contas_pagar_pagamentos (
    id, tenant_id, filial_id, conta_pagar_id, valor, usuario_id, pago_em
)
SELECT
    MD5(conta.id::text || ':pagamento-integral-v39')::UUID,
    conta.tenant_id,
    conta.filial_id,
    conta.id,
    conta.valor_pago,
    conta.usuario_id,
    COALESCE(conta.pago_em, conta.atualizado_em)
FROM contas_pagar conta
WHERE conta.status = 'PAGO'
  AND conta.valor_pago > 0;
