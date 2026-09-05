ALTER TABLE contas_receber DROP CONSTRAINT ck_contas_receber_status;
ALTER TABLE contas_receber
    ADD CONSTRAINT ck_contas_receber_status
    CHECK (status IN ('ABERTO', 'PARCIAL', 'RECEBIDO', 'CANCELADO'));

ALTER TABLE contas_receber
    ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;

ALTER TABLE contas_receber
    ADD CONSTRAINT uq_contas_receber_tenant_id_id UNIQUE (tenant_id, id);

CREATE TABLE contas_receber_recebimentos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL REFERENCES filiais(id),
    conta_receber_id UUID NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    usuario_id UUID,
    recebido_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_contas_receber_recebimentos_valor CHECK (valor > 0),
    CONSTRAINT fk_contas_receber_recebimentos_conta_tenant
        FOREIGN KEY (tenant_id, conta_receber_id)
        REFERENCES contas_receber (tenant_id, id)
);

CREATE INDEX idx_contas_receber_recebimentos_tenant_conta
    ON contas_receber_recebimentos (tenant_id, conta_receber_id, recebido_em DESC);
