package com.traxup.tplug.erp.contabilidade;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FilialContabilidadeApplicationService {
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;
    private final EscopoFilialContabilidade escopoFilial;

    public FilialContabilidadeApplicationService(
            JdbcTemplate jdbc, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.auditoria = auditoria;
        this.escopoFilial = new EscopoFilialContabilidade(jdbc);
    }

    @Transactional(readOnly = true)
    public List<FilialDisponivel> listar(UUID tenantId, UUID usuarioId) {
        var escopo = escopoFilial.resolver(tenantId, usuarioId, null);
        List<FilialDisponivel> filiais = jdbc.query("""
                SELECT f.id, f.empresa_id, e.razao_social AS empresa_nome,
                       f.nome, f.cnpj
                FROM filiais f
                JOIN empresas e
                  ON e.tenant_id = f.tenant_id
                 AND e.id = f.empresa_id
                WHERE f.tenant_id = ?
                  AND f.ativo = TRUE
                  AND e.ativo = TRUE
                  AND (CAST(? AS BOOLEAN) = TRUE OR EXISTS (
                      SELECT 1
                      FROM usuario_filiais uf
                      WHERE uf.tenant_id = f.tenant_id
                        AND uf.usuario_id = ?
                        AND uf.filial_id = f.id
                  ))
                ORDER BY e.razao_social, f.nome, f.id
                """, (rs, n) -> new FilialDisponivel(
                        rs.getObject("id", UUID.class),
                        rs.getObject("empresa_id", UUID.class),
                        rs.getString("empresa_nome"),
                        rs.getString("nome"),
                        rs.getString("cnpj")),
                tenantId, escopo.acessoTotal(), usuarioId);

        auditoria.registrar(tenantId, usuarioId, null, null,
                "LISTAR_FILIAIS_CONTABILIDADE", "FILIAL_CONTABILIDADE",
                UUID.randomUUID(), "acessoTotal=" + escopo.acessoTotal()
                        + ";resultados=" + filiais.size());
        return filiais;
    }

    public record FilialDisponivel(
            UUID id,
            UUID empresaId,
            String empresaNome,
            String nome,
            String cnpj) {}
}
