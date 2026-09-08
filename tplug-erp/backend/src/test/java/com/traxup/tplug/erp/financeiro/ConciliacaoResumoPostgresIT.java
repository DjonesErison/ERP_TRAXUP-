package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ConciliacaoResumoPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConciliacaoLancamentoRepository repository;

    @Test
    void deveResumirStatusNaturezasEIsolarTenant() {
        Contexto tenantA = criarContexto("A");
        Contexto tenantB = criarContexto("B");
        Instant instante = Instant.parse("2026-09-08T12:00:00Z");

        inserirPendente(tenantA, "NORMAL", new BigDecimal("100.00"), instante, "A-NORMAL");
        inserirPendente(tenantA, "TAXA", new BigDecimal("10.00"), instante, "A-TAXA");
        inserirConciliado(tenantA, "ANTECIPACAO", new BigDecimal("200.00"), instante, "A-ANT");
        inserirPendente(tenantA, "ESTORNO", new BigDecimal("30.00"), instante, "A-EST");
        inserirPendente(tenantA, "CHARGEBACK", new BigDecimal("40.00"), instante, "A-CHG");

        inserirPendente(tenantB, "TAXA", new BigDecimal("999.00"), instante, "B-IGNORAR");

        ConciliacaoResumoProjection resumo = repository.resumir(tenantA.tenantId(), tenantA.contaId());

        assertEquals(5L, resumo.getTotalLancamentos());
        assertValor("380.00", resumo.getValorTotal());
        assertEquals(4L, resumo.getPendentes());
        assertValor("180.00", resumo.getValorPendente());
        assertEquals(1L, resumo.getConciliados());
        assertValor("200.00", resumo.getValorConciliado());
        assertEquals(1L, resumo.getTaxas());
        assertValor("10.00", resumo.getValorTaxas());
        assertEquals(1L, resumo.getAntecipacoes());
        assertValor("200.00", resumo.getValorAntecipacoes());
        assertEquals(1L, resumo.getEstornos());
        assertValor("30.00", resumo.getValorEstornos());
        assertEquals(1L, resumo.getChargebacks());
        assertValor("40.00", resumo.getValorChargebacks());
    }

    private Contexto criarContexto(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant Resumo " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa Resumo " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial Resumo " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, 'BANCO', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId, "Conta Resumo " + sufixo);
        return new Contexto(tenantId, filialId, contaId);
    }

    private void inserirPendente(Contexto contexto, String natureza, BigDecimal valor,
                                 Instant ocorridoEm, String referencia) {
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, natureza, ocorrido_em, status, criado_em)
                VALUES (?, ?, ?, ?, 'TESTE', ?, 'ENTRADA', ?, 'Resumo operacional', ?, ?,
                        'PENDENTE', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), contexto.tenantId(), contexto.filialId(), contexto.contaId(),
                referencia, valor, natureza, ocorridoEm);
    }

    private void inserirConciliado(Contexto contexto, String natureza, BigDecimal valor,
                                   Instant ocorridoEm, String referencia) {
        UUID movimentoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras_movimentos
                    (id, tenant_id, filial_id, conta_financeira_id, tipo, valor, descricao, ocorrido_em)
                VALUES (?, ?, ?, ?, 'ENTRADA', ?, 'Movimento resumo', ?)
                """, movimentoId, contexto.tenantId(), contexto.filialId(), contexto.contaId(), valor, ocorridoEm);
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, natureza, ocorrido_em, status, movimento_id,
                     criado_em, conciliado_em)
                VALUES (?, ?, ?, ?, 'TESTE', ?, 'ENTRADA', ?, 'Resumo operacional', ?, ?,
                        'CONCILIADO', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), contexto.tenantId(), contexto.filialId(), contexto.contaId(),
                referencia, valor, natureza, ocorridoEm, movimentoId);
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual));
    }

    private record Contexto(UUID tenantId, UUID filialId, UUID contaId) {}
}
