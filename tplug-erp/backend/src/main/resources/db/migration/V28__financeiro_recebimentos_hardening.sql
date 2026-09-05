ALTER TABLE filiais
    ADD CONSTRAINT uk_filiais_tenant_id UNIQUE (tenant_id, id);

ALTER TABLE contas_receber
    DROP CONSTRAINT contas_receber_filial_id_fkey,
    DROP CONSTRAINT contas_receber_cliente_id_fkey;

ALTER TABLE contas_receber
    ADD CONSTRAINT fk_contas_receber_tenant_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    ADD CONSTRAINT fk_contas_receber_tenant_cliente
        FOREIGN KEY (tenant_id, cliente_id)
        REFERENCES pessoas (tenant_id, id);

ALTER TABLE contas_receber_recebimentos
    DROP CONSTRAINT contas_receber_recebimentos_filial_id_fkey;

ALTER TABLE contas_receber_recebimentos
    ADD CONSTRAINT fk_contas_receber_recebimentos_tenant_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id);

INSERT INTO contas_receber_recebimentos (
    id, tenant_id, filial_id, conta_receber_id, valor, usuario_id, recebido_em
)
SELECT
    MD5(conta.id::text || ':recebimento-integral-v28')::UUID,
    conta.tenant_id,
    conta.filial_id,
    conta.id,
    conta.valor_recebido,
    conta.usuario_id,
    COALESCE(conta.recebido_em, conta.atualizado_em)
FROM contas_receber conta
WHERE conta.status = 'RECEBIDO'
  AND conta.valor_recebido > 0
  AND NOT EXISTS (
      SELECT 1
      FROM contas_receber_recebimentos recebimento
      WHERE recebimento.tenant_id = conta.tenant_id
        AND recebimento.conta_receber_id = conta.id
  );
