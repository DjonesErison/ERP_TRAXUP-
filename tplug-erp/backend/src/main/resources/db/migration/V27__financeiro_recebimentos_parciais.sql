ALTER TABLE contas_receber
    DROP CONSTRAINT ck_contas_receber_status;

ALTER TABLE contas_receber
    ADD CONSTRAINT ck_contas_receber_status
        CHECK (status IN ('ABERTO', 'PARCIALMENTE_RECEBIDO', 'RECEBIDO', 'CANCELADO'));

ALTER TABLE contas_receber
    ADD CONSTRAINT uk_contas_receber_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE contas_receber_movimentos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    conta_receber_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    data_movimento DATE NOT NULL,
    observacao VARCHAR(500),
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_contas_receber_movimentos_conta
        FOREIGN KEY (tenant_id, conta_receber_id)
        REFERENCES contas_receber (tenant_id, id),
    CONSTRAINT ck_contas_receber_movimentos_tipo
        CHECK (tipo IN ('RECEBIMENTO')),
    CONSTRAINT ck_contas_receber_movimentos_valor
        CHECK (valor > 0)
);

CREATE INDEX idx_contas_receber_movimentos_conta_data
    ON contas_receber_movimentos (tenant_id, conta_receber_id, data_movimento DESC, criado_em DESC);

INSERT INTO contas_receber_movimentos (
    id, tenant_id, conta_receber_id, tipo, valor, data_movimento, observacao, usuario_id, criado_em
)
SELECT
    MD5(id::text || ':recebimento-v27')::UUID,
    tenant_id,
    id,
    'RECEBIMENTO',
    valor_recebido,
    COALESCE(recebido_em::DATE, atualizado_em::DATE),
    'Historico migrado da baixa integral anterior a V27',
    usuario_id,
    COALESCE(recebido_em, atualizado_em)
FROM contas_receber
WHERE status = 'RECEBIDO'
  AND valor_recebido > 0;
