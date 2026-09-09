package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class FiscalDocumentoApplicationService {
    private final FiscalSolicitacaoRepository solicitacoes;
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalDocumentoApplicationService(FiscalSolicitacaoRepository solicitacoes,
                                             JdbcTemplate jdbc,
                                             AuditoriaApplicationService auditoria) {
        this.solicitacoes = solicitacoes;
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado estruturar(UUID tenantId, UUID usuarioId, UUID solicitacaoId) {
        FiscalSolicitacao solicitacao = solicitacoes.findByIdAndTenantId(solicitacaoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Solicitacao fiscal nao encontrada para o tenant informado"));
        if (!"PROCESSANDO".equals(solicitacao.getStatus())) {
            throw new IllegalArgumentException("Documento fiscal exige solicitacao PROCESSANDO");
        }

        int inseridos = jdbc.update("""
                INSERT INTO fiscal_documentos (
                    id, tenant_id, solicitacao_id, filial_id, pedido_venda_id,
                    modelo, ambiente, status, quantidade_itens,
                    valor_bruto, valor_desconto, valor_total)
                SELECT CAST(md5(s.id::text) AS UUID), s.tenant_id, s.id, s.filial_id,
                    s.pedido_venda_id, s.modelo, s.ambiente, 'ESTRUTURADO',
                    COUNT(*)::INTEGER,
                    SUM(i.quantidade * (i.preco_unitario + i.adicional_combo_unitario)),
                    SUM(i.desconto_valor), SUM(i.total_item)
                FROM fiscal_solicitacoes s
                JOIN fiscal_solicitacao_itens i
                  ON i.tenant_id = s.tenant_id AND i.solicitacao_id = s.id
                WHERE s.tenant_id = ? AND s.id = ?
                GROUP BY s.id, s.tenant_id, s.filial_id, s.pedido_venda_id, s.modelo, s.ambiente
                ON CONFLICT (tenant_id, solicitacao_id) DO NOTHING
                """, tenantId, solicitacaoId);

        ResultadoBase base = jdbc.queryForObject("""
                SELECT id, quantidade_itens, valor_bruto, valor_desconto, valor_total
                FROM fiscal_documentos
                WHERE tenant_id = ? AND solicitacao_id = ?
                """, (rs, rowNum) -> new ResultadoBase(
                        rs.getObject("id", UUID.class),
                        rs.getInt("quantidade_itens"),
                        rs.getBigDecimal("valor_bruto"),
                        rs.getBigDecimal("valor_desconto"),
                        rs.getBigDecimal("valor_total")), tenantId, solicitacaoId);
        if (base == null) {
            throw new IllegalArgumentException("Snapshot de itens deve ser gerado antes do documento fiscal");
        }
        if (inseridos > 0) {
            auditoria.registrar(tenantId, usuarioId, null, solicitacao.getFilialId(),
                    "ESTRUTURAR_DOCUMENTO", "FISCAL_DOCUMENTO", base.id(),
                    "solicitacaoId=" + solicitacaoId + ";itens=" + base.quantidadeItens()
                            + ";total=" + base.valorTotal());
        }
        return new Resultado(base.id(), base.quantidadeItens(), base.valorBruto(),
                base.valorDesconto(), base.valorTotal(), inseridos == 0);
    }

    private record ResultadoBase(UUID id, int quantidadeItens, BigDecimal valorBruto,
                                 BigDecimal valorDesconto, BigDecimal valorTotal) {}

    public record Resultado(UUID documentoId, int quantidadeItens, BigDecimal valorBruto,
                            BigDecimal valorDesconto, BigDecimal valorTotal, boolean repetida) {}
}
