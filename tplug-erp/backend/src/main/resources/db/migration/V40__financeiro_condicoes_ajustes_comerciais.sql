ALTER TABLE condicoes_pagamento
    ADD COLUMN juros_tipo VARCHAR(20),
    ADD COLUMN juros_valor NUMERIC(15,4),
    ADD COLUMN desconto_tipo VARCHAR(20),
    ADD COLUMN desconto_valor NUMERIC(15,4),
    ADD COLUMN entrada_tipo VARCHAR(20),
    ADD COLUMN entrada_valor NUMERIC(15,4);

ALTER TABLE condicoes_pagamento
    ADD CONSTRAINT ck_condicoes_pagamento_juros
        CHECK (
            (juros_tipo IS NULL AND juros_valor IS NULL)
            OR (juros_tipo IS NOT NULL AND juros_valor IS NOT NULL
                AND juros_tipo IN ('PERCENTUAL', 'VALOR_FIXO')
                AND juros_valor > 0)
        ),
    ADD CONSTRAINT ck_condicoes_pagamento_desconto
        CHECK (
            (desconto_tipo IS NULL AND desconto_valor IS NULL)
            OR (desconto_tipo IS NOT NULL AND desconto_valor IS NOT NULL
                AND desconto_tipo IN ('PERCENTUAL', 'VALOR_FIXO')
                AND desconto_valor > 0
                AND (desconto_tipo <> 'PERCENTUAL' OR desconto_valor <= 100))
        ),
    ADD CONSTRAINT ck_condicoes_pagamento_entrada
        CHECK (
            (entrada_tipo IS NULL AND entrada_valor IS NULL)
            OR (entrada_tipo IS NOT NULL AND entrada_valor IS NOT NULL
                AND entrada_tipo IN ('PERCENTUAL', 'VALOR_FIXO')
                AND entrada_valor > 0
                AND (entrada_tipo <> 'PERCENTUAL' OR entrada_valor <= 100))
        );
