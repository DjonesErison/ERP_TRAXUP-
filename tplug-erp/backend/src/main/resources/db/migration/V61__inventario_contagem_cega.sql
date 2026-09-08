ALTER TABLE inventario_sessoes
    ADD COLUMN contagem_cega BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN inventario_sessoes.contagem_cega IS
    'Quando true, oculta saldo do sistema e divergencia enquanto a sessao estiver ABERTA.';
