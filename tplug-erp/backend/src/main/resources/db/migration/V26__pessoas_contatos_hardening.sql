ALTER TABLE pessoas
    ADD CONSTRAINT uk_pessoas_tenant_id UNIQUE (tenant_id, id);

ALTER TABLE pessoa_contatos
    DROP CONSTRAINT pessoa_contatos_pessoa_id_fkey;

ALTER TABLE pessoa_contatos
    ADD CONSTRAINT fk_pessoa_contatos_tenant_pessoa
        FOREIGN KEY (tenant_id, pessoa_id)
        REFERENCES pessoas (tenant_id, id);

ALTER TABLE pessoa_contatos
    ADD CONSTRAINT ck_pessoa_contatos_canal
        CHECK (
            NULLIF(BTRIM(email), '') IS NOT NULL
            OR NULLIF(BTRIM(telefone), '') IS NOT NULL
        );

WITH contatos_principais_duplicados AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY tenant_id, pessoa_id
               ORDER BY criado_em, id
           ) AS ordem
    FROM pessoa_contatos
    WHERE principal
)
UPDATE pessoa_contatos contato
SET principal = FALSE,
    atualizado_em = CURRENT_TIMESTAMP
FROM contatos_principais_duplicados duplicado
WHERE contato.id = duplicado.id
  AND duplicado.ordem > 1;

CREATE UNIQUE INDEX uk_pessoa_contatos_principal
    ON pessoa_contatos (tenant_id, pessoa_id)
    WHERE principal;
