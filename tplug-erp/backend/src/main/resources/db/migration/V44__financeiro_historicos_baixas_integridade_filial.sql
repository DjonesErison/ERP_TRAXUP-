ALTER TABLE contas_receber
    ADD CONSTRAINT uq_contas_receber_tenant_filial_id
        UNIQUE (tenant_id, filial_id, id);

ALTER TABLE contas_receber_recebimentos
    DROP CONSTRAINT fk_contas_receber_recebimentos_conta_tenant;

ALTER TABLE contas_receber_recebimentos
    ADD CONSTRAINT fk_contas_receber_recebimentos_conta_tenant_filial
        FOREIGN KEY (tenant_id, filial_id, conta_receber_id)
        REFERENCES contas_receber (tenant_id, filial_id, id);

ALTER TABLE contas_pagar
    ADD CONSTRAINT uq_contas_pagar_tenant_filial_id
        UNIQUE (tenant_id, filial_id, id);

ALTER TABLE contas_pagar_pagamentos
    DROP CONSTRAINT fk_contas_pagar_pagamentos_conta_tenant;

ALTER TABLE contas_pagar_pagamentos
    ADD CONSTRAINT fk_contas_pagar_pagamentos_conta_tenant_filial
        FOREIGN KEY (tenant_id, filial_id, conta_pagar_id)
        REFERENCES contas_pagar (tenant_id, filial_id, id);
