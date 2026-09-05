INSERT INTO permissoes (id, chave, descricao)
VALUES
    ('10000000-0000-0000-0000-000000000011', 'AUDITORIA_LER', 'Permite consultar eventos de auditoria do tenant')
ON CONFLICT (chave) DO NOTHING;
