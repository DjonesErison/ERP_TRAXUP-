ALTER TABLE usuarios
    ADD CONSTRAINT uq_usuarios_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE contas_receber_recebimentos
    ADD CONSTRAINT fk_contas_receber_recebimentos_tenant_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);

ALTER TABLE contas_pagar_pagamentos
    ADD CONSTRAINT fk_contas_pagar_pagamentos_tenant_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id);
