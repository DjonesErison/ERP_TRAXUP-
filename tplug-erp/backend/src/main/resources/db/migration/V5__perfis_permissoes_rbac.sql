ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_tenant_id
        UNIQUE (tenant_id, id);

CREATE TABLE permissoes (
    id UUID PRIMARY KEY,
    chave VARCHAR(120) NOT NULL,
    descricao VARCHAR(255),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_permissoes_chave UNIQUE (chave)
);

CREATE TABLE perfis (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_perfis_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT uk_perfis_tenant_nome
        UNIQUE (tenant_id, nome),

    CONSTRAINT uk_perfis_tenant_id
        UNIQUE (tenant_id, id)
);

CREATE INDEX idx_perfis_tenant
    ON perfis (tenant_id);

CREATE TABLE usuario_perfis (
    tenant_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    perfil_id UUID NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (tenant_id, usuario_id, perfil_id),

    CONSTRAINT fk_usuario_perfis_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_usuario_perfis_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id),

    CONSTRAINT fk_usuario_perfis_perfil
        FOREIGN KEY (tenant_id, perfil_id)
        REFERENCES perfis (tenant_id, id)
);

CREATE INDEX idx_usuario_perfis_usuario
    ON usuario_perfis (tenant_id, usuario_id);

CREATE INDEX idx_usuario_perfis_perfil
    ON usuario_perfis (tenant_id, perfil_id);

CREATE TABLE perfil_permissoes (
    tenant_id UUID NOT NULL,
    perfil_id UUID NOT NULL,
    permissao_id UUID NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (tenant_id, perfil_id, permissao_id),

    CONSTRAINT fk_perfil_permissoes_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_perfil_permissoes_perfil
        FOREIGN KEY (tenant_id, perfil_id)
        REFERENCES perfis (tenant_id, id),

    CONSTRAINT fk_perfil_permissoes_permissao
        FOREIGN KEY (permissao_id)
        REFERENCES permissoes(id)
);

CREATE INDEX idx_perfil_permissoes_perfil
    ON perfil_permissoes (tenant_id, perfil_id);

CREATE INDEX idx_perfil_permissoes_permissao
    ON perfil_permissoes (permissao_id);
