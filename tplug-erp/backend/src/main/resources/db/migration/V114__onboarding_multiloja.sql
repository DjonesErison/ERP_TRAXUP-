ALTER TABLE filiais ADD COLUMN codigo VARCHAR(20);
ALTER TABLE filiais ADD COLUMN matriz BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE filiais ADD COLUMN cep VARCHAR(9);
ALTER TABLE filiais ADD COLUMN endereco VARCHAR(200);
ALTER TABLE filiais ADD COLUMN numero VARCHAR(20);
ALTER TABLE filiais ADD COLUMN complemento VARCHAR(100);
ALTER TABLE filiais ADD COLUMN bairro VARCHAR(100);
ALTER TABLE filiais ADD COLUMN cidade VARCHAR(100);
ALTER TABLE filiais ADD COLUMN uf VARCHAR(2);
ALTER TABLE filiais ADD COLUMN estoque_ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE filiais ADD COLUMN pdv_ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE filiais ADD COLUMN fiscal_ativo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE filiais ADD COLUMN configuracao_concluida BOOLEAN NOT NULL DEFAULT FALSE;

WITH numeradas AS (
    SELECT id, tenant_id, row_number() OVER (PARTITION BY tenant_id ORDER BY criado_em, id) AS numero
      FROM filiais
)
UPDATE filiais f
   SET codigo = lpad(n.numero::text, 3, '0'),
       matriz = n.numero = 1
  FROM numeradas n
 WHERE n.id = f.id;

ALTER TABLE filiais ALTER COLUMN codigo SET NOT NULL;
CREATE UNIQUE INDEX uq_filiais_tenant_codigo ON filiais (tenant_id, codigo);
