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
class ConciliacaoResumoFiltradoPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConciliacaoLancamentoRepository repository;

    @Test
    void deveAplicarFiltrosSemVazarOutroTenant() {
        Contexto tenantA = criarContexto("A");
        Contexto tenantB = criarContexto("B");
        Instant dia1 = Instant.parse("2026-09-01T12:00:00Z");
        Instant dia2 = Instant.parse("2026-09-02T12:00:00Z");
        Instant dia3 = Instant.parse("2026-09-03T12:00:00Z");

        inserir(tenantA, "OFX", "TAXA", "PENDENTE", new BigDecimal("10.00"), dia1, "A-1");
        inserir(tenantA, "OFX", "NORMAL", "PENDENTE", new BigDecimal("20.00"), dia2, "A-2");
        inserir(tenantA, "API", "TAXA", "PENDENTE", new BigDecimal("30.00"), dia3, "A-3");
        inserir(tenantB, "OFX", "TAXA", "PENDENTE", new BigDecimal("999.00"), dia2, "B-1");

        ConciliacaoResumoProjection resumo = repository.resumirFiltrado(
                tenantA.tenantId(), tenantA.contaId(), "OFX", "TAXA", "PENDENTE", dia1, dia2);

        assertEquals(1L, resumo.getTotalLancamentos());
        assertValor("10.00", resumo.getValorTotal());
        assertEquals(1L, resumo.getPendentes());
        assertValor("10.00", resumo.getValorPendente());
        assertEquals(1L, resumo.getTaxas());
        assertValor("10.00", resumo.getValorTaxas());
    }

    private Contexto criarContexto(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant Resumo Filtro " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa Resumo Filtro " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial Resumo Filtro " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, 'BANCO', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId, "Conta Resumo Filtro " + sufixo);
        return new Contexto(tenantId, filialId, contaId);
    }

    private void inserir(Contexto contexto, String origem, String natureza, String status, BigDecimal valor,
                         Instant ocorridoEm, String referencia) {
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, natureza, ocorrido_em, status, criado_em)
                VALUES (?, ?, ?, ?, ?, ?, 'ENTRADA', ?, 'Resumo filtrado', ?, ?, ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), contexto.tenantId(), contexto.filialId(), contexto.contaId(), origem,
                referencia, valor, natureza, ocorridoEm, status);
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual));
    }

    private record Contexto(UUID tenantId, UUID filialId, UUID contaId) {}
}
