-- A transactional counter serializes concurrent allocations. Rollbacks do not burn
-- the finite namespace, and codes are never reused after tenant deletion.
CREATE TABLE tenant_codigo_empresa_allocator (
    singleton boolean PRIMARY KEY DEFAULT true CHECK (singleton),
    proximo integer NOT NULL CHECK (proximo BETWEEN 0 AND 10000)
);
INSERT INTO tenant_codigo_empresa_allocator VALUES (true, 0);

CREATE FUNCTION gerar_codigo_empresa() RETURNS varchar(4) LANGUAGE plpgsql AS $$
DECLARE numero integer;
BEGIN
    UPDATE tenant_codigo_empresa_allocator SET proximo = proximo + 1
      WHERE singleton AND proximo < 10000 RETURNING proximo - 1 INTO numero;
    IF numero IS NULL THEN
        RAISE EXCEPTION 'Limite de codigos de empresa atingido (10000)' USING ERRCODE = '54000';
    END IF;
    RETURN lpad(numero::text, 4, '0');
END;
$$;

ALTER TABLE tenants ADD COLUMN codigo_empresa varchar(4);
UPDATE tenants SET codigo_empresa = gerar_codigo_empresa();
ALTER TABLE tenants ALTER COLUMN codigo_empresa SET DEFAULT gerar_codigo_empresa();
ALTER TABLE tenants ALTER COLUMN codigo_empresa SET NOT NULL;
ALTER TABLE tenants ADD CONSTRAINT ck_tenants_codigo_empresa CHECK (codigo_empresa ~ '^[0-9]{4}$');
ALTER TABLE tenants ADD CONSTRAINT uk_tenants_codigo_empresa UNIQUE (codigo_empresa);
