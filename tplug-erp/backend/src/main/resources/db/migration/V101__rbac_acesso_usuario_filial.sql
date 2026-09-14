CREATE TABLE usuario_filiais (
    tenant_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    criado_por_id UUID,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, usuario_id, filial_id),
    CONSTRAINT fk_usuario_filial_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id),
    CONSTRAINT fk_usuario_filial_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_usuario_filial_criado_por
        FOREIGN KEY (tenant_id, criado_por_id)
        REFERENCES usuarios (tenant_id, id)
);

CREATE INDEX idx_usuario_filiais_filial
    ON usuario_filiais (tenant_id, filial_id, usuario_id);

INSERT INTO usuario_filiais (tenant_id, usuario_id, filial_id)
SELECT DISTINCT up.tenant_id, up.usuario_id, f.id
FROM usuario_perfis up
JOIN perfis p
  ON p.tenant_id = up.tenant_id AND p.id = up.perfil_id
JOIN filiais f
  ON f.tenant_id = up.tenant_id AND f.ativo = TRUE
WHERE UPPER(p.nome) = 'CONTABILIDADE'
ON CONFLICT DO NOTHING;

COMMENT ON TABLE usuario_filiais IS
    'Filiais que um usuario nao administrativo esta autorizado a consultar';
