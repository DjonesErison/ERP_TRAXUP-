package com.traxup.tplug.erp.contabilidade;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

final class SpedFilialTestFixture {
    private SpedFilialTestFixture() {}

    static Contexto criar(JdbcTemplate jdbc, UUID tenantId, String sufixo) {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID perfilId = UUID.randomUUID();

        jdbc.update("""
                INSERT INTO empresas (id, tenant_id, razao_social)
                VALUES (?, ?, ?)
                """, empresaId, tenantId, "Empresa SPED " + sufixo);
        jdbc.update("""
                INSERT INTO filiais (id, tenant_id, empresa_id, nome)
                VALUES (?, ?, ?, ?)
                """, filialId, tenantId, empresaId, "Filial SPED " + sufixo);
        jdbc.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash)
                VALUES (?, ?, ?, ?, 'hash-teste')
                """, usuarioId, tenantId, "Admin SPED " + sufixo,
                usuarioId + "@teste.local");
        jdbc.update("""
                INSERT INTO perfis
                    (id, tenant_id, nome, descricao, ativo)
                VALUES (?, ?, 'ADMIN', ?, TRUE)
                """, perfilId, tenantId, "Admin SPED " + sufixo);
        jdbc.update("""
                INSERT INTO usuario_perfis
                    (tenant_id, usuario_id, perfil_id)
                VALUES (?, ?, ?)
                """, tenantId, usuarioId, perfilId);

        return new Contexto(usuarioId, filialId);
    }

    record Contexto(UUID usuarioId, UUID filialId) {}
}
