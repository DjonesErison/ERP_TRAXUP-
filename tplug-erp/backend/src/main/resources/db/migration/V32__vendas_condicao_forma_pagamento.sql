ALTER TABLE pedidos_venda
    ADD COLUMN forma_pagamento_id UUID,
    ADD COLUMN condicao_pagamento_id UUID;

ALTER TABLE pedidos_venda
    ADD CONSTRAINT fk_pedidos_venda_forma_pagamento_tenant
        FOREIGN KEY (tenant_id, forma_pagamento_id)
        REFERENCES formas_pagamento (tenant_id, id),
    ADD CONSTRAINT fk_pedidos_venda_condicao_pagamento_tenant
        FOREIGN KEY (tenant_id, condicao_pagamento_id)
        REFERENCES condicoes_pagamento (tenant_id, id);

CREATE INDEX idx_pedidos_venda_tenant_forma_pagamento
    ON pedidos_venda (tenant_id, forma_pagamento_id);
CREATE INDEX idx_pedidos_venda_tenant_condicao_pagamento
    ON pedidos_venda (tenant_id, condicao_pagamento_id);
