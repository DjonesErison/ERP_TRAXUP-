package com.traxup.tplug.erp.contabilidade;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Resolve o alcance de filial das consultas contabeis.
 *
 * <p>Administradores mantem visao completa do tenant. Demais usuarios
 * somente podem consultar filiais vinculadas em {@code usuario_filiais}.</p>
 */
public final class EscopoFilialContabilidade {
    private final JdbcTemplate jdbc;

    public EscopoFilialContabilidade(JdbcTemplate jdbc) {
        Assert.notNull(jdbc, "JdbcTemplate e obrigatorio");
        this.jdbc = jdbc;
    }

    public Escopo resolver(UUID tenantId, UUID usuarioId,
                           UUID filialSolicitada) {
        if (tenantId == null || usuarioId == null)
            throw new AccessDeniedException(
                    "Usuario autenticado e tenant sao obrigatorios");

        Boolean administrador = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM usuario_perfis up
                    JOIN perfis p
                      ON p.tenant_id = up.tenant_id
                     AND p.id = up.perfil_id
                    WHERE up.tenant_id = ?
                      AND up.usuario_id = ?
                      AND p.ativo = TRUE
                      AND UPPER(p.nome) = 'ADMIN'
                )
                """, Boolean.class, tenantId, usuarioId);
        boolean acessoTotal = Boolean.TRUE.equals(administrador);

        if (!acessoTotal && filialSolicitada != null) {
            Boolean autorizado = jdbc.queryForObject("""
                    SELECT EXISTS (
                        SELECT 1
                        FROM usuario_filiais uf
                        WHERE uf.tenant_id = ?
                          AND uf.usuario_id = ?
                          AND uf.filial_id = ?
                    )
                    """, Boolean.class,
                    tenantId, usuarioId, filialSolicitada);
            if (!Boolean.TRUE.equals(autorizado))
                throw new AccessDeniedException(
                        "Usuario sem acesso a filial solicitada");
        }

        return new Escopo(acessoTotal, tenantId, usuarioId,
                filialSolicitada);
    }

    public record Escopo(
            boolean acessoTotal,
            UUID tenantId,
            UUID usuarioId,
            UUID filialId) {}
}
