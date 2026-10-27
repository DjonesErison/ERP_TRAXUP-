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
                SELECT f.id, f.empresa_id, e.nome_fantasia AS empresa_nome, f.nome, f.cnpj,
                       f.codigo, f.matriz, f.cidade, f.uf,
                       (f.configuracao_concluida = true
                        AND nullif(trim(f.nome),'') IS NOT NULL
                        AND nullif(trim(f.cep),'') IS NOT NULL
                        AND nullif(trim(f.endereco),'') IS NOT NULL
                        AND nullif(trim(f.numero),'') IS NOT NULL
                        AND nullif(trim(f.bairro),'') IS NOT NULL
                        AND nullif(trim(f.cidade),'') IS NOT NULL
                        AND nullif(trim(f.uf),'') IS NOT NULL) AS dados_empresa_completos,
                       (f.fiscal_ativo = false OR EXISTS(
                          SELECT 1 FROM fiscal_perfis_filial fp
                           WHERE fp.tenant_id=f.tenant_id AND fp.filial_id=f.id AND fp.ativo=true
                       )) AS fiscal_completo,
                       coalesce(oc.equipe_revisada,false) AS usuarios_completos,
                       (f.pdv_ativo = false OR EXISTS(
                          SELECT 1 FROM pdv_configuracoes pc
                          JOIN pdv_terminais pt ON pt.tenant_id=pc.tenant_id AND pt.id=pc.terminal_id
                           WHERE pc.tenant_id=f.tenant_id AND pc.filial_id=f.id AND pt.ativo=true
                       )) AS pdv_completo
                  FROM usuario_filiais uf
                  JOIN filiais f ON f.id = uf.filial_id AND f.tenant_id = uf.tenant_id
                  JOIN empresas e ON e.id = f.empresa_id AND e.tenant_id = f.tenant_id
                  LEFT JOIN onboarding_configuracoes oc ON oc.tenant_id=f.tenant_id
                 WHERE uf.tenant_id = ?
                   AND uf.usuario_id = ?
                   AND f.ativo = true
                 ORDER BY f.nome, f.id
                """,
                (rs, rowNum) -> new FilialPermitidaResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("empresa_id", UUID.class),
                        rs.getString("empresa_nome"),
                        rs.getString("nome"),
                        rs.getString("cnpj"),
                        rs.getString("codigo"),
                        rs.getBoolean("matriz"),
                        rs.getString("cidade"),
                        rs.getString("uf"),
                        rs.getBoolean("dados_empresa_completos"),
                        rs.getBoolean("fiscal_completo"),
                        rs.getBoolean("usuarios_completos"),
                        rs.getBoolean("pdv_completo")),
                tenantId, usuarioId);
    }

    public record FilialPermitidaResponse(UUID id, UUID empresaId, String empresaNome, String nome,
                                          String cnpj, String codigo, boolean matriz, String cidade, String uf,
                                          boolean dadosEmpresaCompletos, boolean fiscalCompleto,
                                          boolean usuariosCompletos, boolean pdvCompleto) {}
}
