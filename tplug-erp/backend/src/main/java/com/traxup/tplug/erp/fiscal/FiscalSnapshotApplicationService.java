package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FiscalSnapshotApplicationService {
    private final FiscalSolicitacaoRepository solicitacoes;
    private final JdbcTemplate jdbc;
    private final AuditoriaApplicationService auditoria;

    public FiscalSnapshotApplicationService(FiscalSolicitacaoRepository solicitacoes,
                                            JdbcTemplate jdbc,
                                            AuditoriaApplicationService auditoria) {
        this.solicitacoes = solicitacoes;
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado gerar(UUID tenantId, UUID usuarioId, UUID solicitacaoId) {
        FiscalSolicitacao solicitacao = solicitacoes.findByIdAndTenantId(solicitacaoId, tenantId)
                .orElseThrow(() -> new com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException(
                        "Solicitacao fiscal nao encontrada para o tenant informado"));
        if (!"PROCESSANDO".equals(solicitacao.getStatus())) {
            throw new IllegalArgumentException("Snapshot fiscal exige solicitacao PROCESSANDO");
        }

        int inseridos = jdbc.update("""
                INSERT INTO fiscal_solicitacao_itens (
                    id, tenant_id, solicitacao_id, pedido_venda_item_id, produto_id, grade_id,
                    codigo, descricao, ncm, unidade, quantidade, preco_unitario,
                    adicional_combo_unitario, desconto_valor, total_item)
                SELECT CAST(md5(s.id::text || i.id::text) AS UUID), s.tenant_id, s.id, i.id,
                    i.produto_id, i.grade_id,
                    COALESCE(g.codigo_grade, p.codigo),
                    COALESCE(g.descricao_grade, p.descricao),
                    p.ncm, p.unidade, i.quantidade, i.preco_unitario,
                    i.adicional_combo_unitario, i.desconto_valor, i.total_item
                FROM fiscal_solicitacoes s
                JOIN pedido_venda_itens i
                  ON i.tenant_id = s.tenant_id AND i.pedido_venda_id = s.pedido_venda_id
                JOIN produtos p
                  ON p.tenant_id = i.tenant_id AND p.id = i.produto_id
                LEFT JOIN grades_produto g
                  ON g.tenant_id = i.tenant_id AND g.id = i.grade_id AND g.produto_id = i.produto_id
                WHERE s.tenant_id = ? AND s.id = ?
                ON CONFLICT (tenant_id, solicitacao_id, pedido_venda_item_id) DO NOTHING
                """, tenantId, solicitacaoId);

        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM fiscal_solicitacao_itens
                WHERE tenant_id = ? AND solicitacao_id = ?
                """, Integer.class, tenantId, solicitacaoId);
        if (total == null || total == 0) {
            throw new IllegalArgumentException("Venda sem itens validos para snapshot fiscal");
        }
        if (inseridos > 0) {
            auditoria.registrar(tenantId, usuarioId, null, solicitacao.getFilialId(),
                    "GERAR_SNAPSHOT_ITENS", "FISCAL_SOLICITACAO", solicitacaoId,
                    "pedidoVendaId=" + solicitacao.getPedidoVendaId() + ";itens=" + total);
        }
        return new Resultado(total, inseridos == 0);
    }

    public record Resultado(int quantidadeItens, boolean repetida) {}
}
