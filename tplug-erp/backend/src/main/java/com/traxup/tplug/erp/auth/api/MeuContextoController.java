package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.auth.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
public class MeuContextoController {
    private final JdbcTemplate jdbc;
    private final TenantContext tenantContext;

    public MeuContextoController(JdbcTemplate jdbc, TenantContext tenantContext) {
        this.jdbc = jdbc;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/filiais")
    public List<FilialPermitidaResponse> filiais() {
        UUID tenantId = tenantContext.tenantId();
        UUID usuarioId = tenantContext.usuarioIdOuNulo();
        if (usuarioId == null) return List.of();

        return jdbc.query("""
                SELECT f.id, f.empresa_id, f.nome, f.cnpj
                  FROM usuario_filiais uf
                  JOIN filiais f ON f.id = uf.filial_id AND f.tenant_id = uf.tenant_id
                 WHERE uf.tenant_id = ?
                   AND uf.usuario_id = ?
                   AND f.ativo = true
                 ORDER BY f.nome, f.id
                """,
                (rs, rowNum) -> new FilialPermitidaResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("empresa_id", UUID.class),
                        rs.getString("nome"),
                        rs.getString("cnpj")),
                tenantId, usuarioId);
    }

    public record FilialPermitidaResponse(UUID id, UUID empresaId, String nome, String cnpj) {}
}
