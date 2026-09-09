package com.traxup.tplug.erp.fiscal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalValidacaoApplicationService {
    private final JdbcTemplate jdbc;

    public FiscalValidacaoApplicationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Resultado validar(UUID tenantId, UUID documentoId) {
        Integer documentos = jdbc.queryForObject("""
                SELECT COUNT(*) FROM fiscal_documentos
                WHERE tenant_id = ? AND id = ?
                """, Integer.class, tenantId, documentoId);
        if (documentos == null || documentos == 0) {
            throw new com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException(
                    "Documento fiscal nao encontrado para o tenant informado");
        }

        Boolean regraAplicada = jdbc.queryForObject("""
                SELECT regra_operacao_id IS NOT NULL
                    AND cfop IS NOT NULL
                    AND ((regime_tributario = 'SIMPLES_NACIONAL' AND csosn IS NOT NULL AND cst_icms IS NULL)
                      OR (regime_tributario = 'REGIME_NORMAL' AND cst_icms IS NOT NULL AND csosn IS NULL))
                FROM fiscal_documentos
                WHERE tenant_id = ? AND id = ?
                """, Boolean.class, tenantId, documentoId);

        Contagens c = jdbc.queryForObject("""
                SELECT COUNT(*)::INTEGER AS itens,
                       COUNT(*) FILTER (WHERE ncm IS NULL OR ncm !~ '^[0-9]{8}$')::INTEGER AS ncm_invalidos,
                       COUNT(*) FILTER (WHERE unidade IS NULL OR btrim(unidade) = '')::INTEGER AS unidades_invalidas,
                       COUNT(*) FILTER (WHERE quantidade <= 0)::INTEGER AS quantidades_invalidas,
                       COUNT(*) FILTER (
                           WHERE total_item <> quantidade * (preco_unitario + adicional_combo_unitario) - desconto_valor
                       )::INTEGER AS totais_invalidos
                FROM fiscal_solicitacao_itens i
                JOIN fiscal_documentos d
                  ON d.tenant_id = i.tenant_id AND d.solicitacao_id = i.solicitacao_id
                WHERE d.tenant_id = ? AND d.id = ?
                """, (rs, n) -> new Contagens(rs.getInt("itens"), rs.getInt("ncm_invalidos"),
                        rs.getInt("unidades_invalidas"), rs.getInt("quantidades_invalidas"),
                        rs.getInt("totais_invalidos")), tenantId, documentoId);

        Boolean totalCoerente = jdbc.queryForObject("""
                SELECT d.valor_total = COALESCE(SUM(i.total_item), 0)
                FROM fiscal_documentos d
                LEFT JOIN fiscal_solicitacao_itens i
                  ON i.tenant_id = d.tenant_id AND i.solicitacao_id = d.solicitacao_id
                WHERE d.tenant_id = ? AND d.id = ?
                GROUP BY d.valor_total
                """, Boolean.class, tenantId, documentoId);

        List<Pendencia> pendencias = new ArrayList<>();
        if (!Boolean.TRUE.equals(regraAplicada)) pendencias.add(new Pendencia("REGRA_FISCAL_NAO_APLICADA", 1));
        if (c == null || c.itens() == 0) pendencias.add(new Pendencia("SEM_ITENS", 1));
        if (c != null && c.ncmInvalidos() > 0) pendencias.add(new Pendencia("NCM_INVALIDO", c.ncmInvalidos()));
        if (c != null && c.unidadesInvalidas() > 0) pendencias.add(new Pendencia("UNIDADE_INVALIDA", c.unidadesInvalidas()));
        if (c != null && c.quantidadesInvalidas() > 0) pendencias.add(new Pendencia("QUANTIDADE_INVALIDA", c.quantidadesInvalidas()));
        if (c != null && c.totaisInvalidos() > 0) pendencias.add(new Pendencia("TOTAL_ITEM_DIVERGENTE", c.totaisInvalidos()));
        if (!Boolean.TRUE.equals(totalCoerente)) pendencias.add(new Pendencia("TOTAL_DOCUMENTO_DIVERGENTE", 1));
        return new Resultado(pendencias.isEmpty(), c == null ? 0 : c.itens(), List.copyOf(pendencias));
    }

    private record Contagens(int itens, int ncmInvalidos, int unidadesInvalidas,
                             int quantidadesInvalidas, int totaisInvalidos) {}
    public record Pendencia(String codigo, int ocorrencias) {}
    public record Resultado(boolean apto, int quantidadeItens, List<Pendencia> pendencias) {}
}
