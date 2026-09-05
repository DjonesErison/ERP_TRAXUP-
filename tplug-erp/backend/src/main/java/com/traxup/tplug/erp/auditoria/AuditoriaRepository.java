package com.traxup.tplug.erp.auditoria;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class AuditoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditoriaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void inserir(AuditoriaEvento evento) {
        jdbcTemplate.update("""
                INSERT INTO auditorias (
                    id, tenant_id, usuario_id, empresa_id, filial_id,
                    operacao, entidade, entidade_id, detalhes, criado_em
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                evento.id(),
                evento.tenantId(),
                evento.usuarioId(),
                evento.empresaId(),
                evento.filialId(),
                evento.operacao(),
                evento.entidade(),
                evento.entidadeId(),
                evento.detalhes(),
                evento.criadoEm());
    }

    public List<AuditoriaEvento> listarPorTenant(UUID tenantId) {
        return jdbcTemplate.query("""
                SELECT id, tenant_id, usuario_id, empresa_id, filial_id,
                       operacao, entidade, entidade_id, detalhes, criado_em
                  FROM auditorias
                 WHERE tenant_id = ?
                 ORDER BY criado_em DESC, id DESC
                """, this::mapear, tenantId);
    }

    private AuditoriaEvento mapear(ResultSet rs, int rowNum) throws SQLException {
        return new AuditoriaEvento(
                rs.getObject("id", UUID.class),
                rs.getObject("tenant_id", UUID.class),
                rs.getObject("usuario_id", UUID.class),
                rs.getObject("empresa_id", UUID.class),
                rs.getObject("filial_id", UUID.class),
                rs.getString("operacao"),
                rs.getString("entidade"),
                rs.getObject("entidade_id", UUID.class),
                rs.getString("detalhes"),
                rs.getObject("criado_em", OffsetDateTime.class));
    }
}
