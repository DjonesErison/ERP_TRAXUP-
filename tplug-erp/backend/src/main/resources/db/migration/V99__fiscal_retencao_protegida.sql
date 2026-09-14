CREATE OR REPLACE FUNCTION proteger_retencao_arquivo_fiscal()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.retencao_ate > CURRENT_DATE THEN
            RAISE EXCEPTION
                'Arquivo fiscal protegido pelo prazo de retencao'
                USING ERRCODE = '23514';
        END IF;
        RETURN OLD;
    END IF;

    IF NEW.retencao_ate < OLD.retencao_ate THEN
        RAISE EXCEPTION
            'Prazo de retencao fiscal nao pode ser reduzido'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_proteger_retencao_arquivo_fiscal
BEFORE UPDATE OF retencao_ate OR DELETE
ON fiscal_arquivos
FOR EACH ROW
EXECUTE FUNCTION proteger_retencao_arquivo_fiscal();
