ALTER TABLE inventario_sessoes
    ADD COLUMN ajustado_por_id UUID,
    ADD COLUMN ajustado_em TIMESTAMPTZ,
    ADD CONSTRAINT fk_inventario_sessao_tenant_ajustado_por FOREIGN KEY (tenant_id, ajustado_por_id)
        REFERENCES usuarios (tenant_id, id),
    ADD CONSTRAINT ck_inventario_sessao_ajuste_consistente CHECK (
        (ajustado_em IS NULL AND ajustado_por_id IS NULL)
        OR (ajustado_em IS NOT NULL)
    );

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000065', 'INVENTARIO_AJUSTAR', 'Aplicar ao estoque as quantidades de inventario concluido')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave = 'INVENTARIO_AJUSTAR'
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
