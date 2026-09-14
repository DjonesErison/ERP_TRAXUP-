ALTER TABLE contabilidade_sped_exportacoes
    ADD COLUMN retencao_ate TIMESTAMPTZ;

UPDATE contabilidade_sped_exportacoes
SET retencao_ate = concluido_em + INTERVAL '5 years'
WHERE status = 'CONCLUIDO';

ALTER TABLE contabilidade_sped_exportacoes
    ADD CONSTRAINT ck_sped_exportacao_retencao CHECK (
        (
            status = 'CONCLUIDO'
            AND retencao_ate IS NOT NULL
            AND retencao_ate >= concluido_em + INTERVAL '5 years'
        )
        OR (
            status <> 'CONCLUIDO'
            AND retencao_ate IS NULL
        )
    );

CREATE OR REPLACE FUNCTION proteger_retencao_sped()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.retencao_ate IS NOT NULL
           AND OLD.retencao_ate > CURRENT_TIMESTAMP THEN
            RAISE EXCEPTION
                'Exportacao SPED protegida pelo prazo de retencao'
                USING ERRCODE = '23514';
        END IF;
        RETURN OLD;
    END IF;

    IF OLD.retencao_ate IS NOT NULL
       AND (
           NEW.retencao_ate IS NULL
           OR NEW.retencao_ate < OLD.retencao_ate
       ) THEN
        RAISE EXCEPTION
            'Prazo de retencao SPED nao pode ser reduzido'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_proteger_retencao_sped
BEFORE UPDATE OF retencao_ate OR DELETE
ON contabilidade_sped_exportacoes
FOR EACH ROW
EXECUTE FUNCTION proteger_retencao_sped();
