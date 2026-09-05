package com.traxup.tplug.erp.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AutorizacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public AutorizacaoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> listarPermissoesEfetivas(UUID tenantId, UUID usuarioId) {
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT p.chave
                  FROM usuario_perfis up
                  JOIN perfil_permissoes pp
                    ON pp.tenant_id = up.tenant_id
                   AND pp.perfil_id = up.perfil_id
                  JOIN permissoes p
                    ON p.id = pp.permissao_id
                  JOIN perfis pf
                    ON pf.tenant_id = up.tenant_id
                   AND pf.id = up.perfil_id
                 WHERE up.tenant_id = ?
                   AND up.usuario_id = ?
                   AND pf.ativo = TRUE
                 ORDER BY p.chave
                """, String.class, tenantId, usuarioId);
    }
}
