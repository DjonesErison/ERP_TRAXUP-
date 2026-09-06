package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ConciliacaoConcurrencyIntegrationTest {

    @Autowired
    private ConciliacaoApplicationService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void importacoesConcorrentesDaMesmaReferenciaDevemSerIdempotentes() throws Exception {
        UUID tenantId = inserirTenant("Tenant Idempotencia Concorrente");
        UUID filialId = inserirFilial(tenantId, "Filial Idempotencia Concorrente");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Idempotencia");
        Instant ocorridoEm = Instant.parse("2026-09-06T04:00:00Z");
        CountDownLatch inicio = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<UUID> primeira = executor.submit(() -> importarAposSinal(
                    inicio, tenantId, contaId, ocorridoEm, "REF-CONCORRENTE"));
            Future<UUID> segunda = executor.submit(() -> importarAposSinal(
                    inicio, tenantId, contaId, ocorridoEm, "REF-CONCORRENTE"));
            inicio.countDown();

            assertEquals(primeira.get(), segunda.get());
        }

        Integer quantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM conciliacao_lancamentos
                WHERE tenant_id = ? AND conta_financeira_id = ?
                  AND origem = 'OFX' AND referencia_externa = 'REF-CONCORRENTE'
                """, Integer.class, tenantId, contaId);
        assertEquals(1, quantidade);
    }

    @Test
    void conciliacoesConcorrentesNaoDevemSobrescreverMovimento() throws Exception {
        UUID tenantId = inserirTenant("Tenant Matching Concorrente");
        UUID filialId = inserirFilial(tenantId, "Filial Matching Concorrente");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Matching");
        UUID movimentoA = inserirMovimento(tenantId, filialId, contaId, "Movimento A");
        UUID movimentoB = inserirMovimento(tenantId, filialId, contaId, "Movimento B");
        UUID lancamentoId = inserirLancamentoPendente(tenantId, filialId, contaId);
        CountDownLatch inicio = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<UUID> primeira = executor.submit(() -> conciliarAposSinal(
                    inicio, tenantId, lancamentoId, movimentoA));
            Future<UUID> segunda = executor.submit(() -> conciliarAposSinal(
                    inicio, tenantId, lancamentoId, movimentoB));
            inicio.countDown();

            int sucessos = 0;
            int conflitos = 0;
            for (Future<UUID> resultado : java.util.List.of(primeira, segunda)) {
                try {
                    resultado.get();
                    sucessos++;
                } catch (ExecutionException exception) {
                    assertInstanceOf(RecursoConflitanteException.class, exception.getCause());
                    conflitos++;
                }
            }
            assertEquals(1, sucessos);
            assertEquals(1, conflitos);
        }

        UUID movimentoPersistido = jdbcTemplate.queryForObject(
                "SELECT movimento_id FROM conciliacao_lancamentos WHERE id = ?",
                UUID.class, lancamentoId);
        assertNotNull(movimentoPersistido);
    }

    private UUID importarAposSinal(CountDownLatch inicio, UUID tenantId, UUID contaId,
                                    Instant ocorridoEm, String referencia) throws InterruptedException {
        inicio.await();
        return service.importar(tenantId, null, contaId, "ofx", referencia, "entrada",
                new BigDecimal("100.00"), "Credito concorrente", ocorridoEm).getId();
    }

    private UUID conciliarAposSinal(CountDownLatch inicio, UUID tenantId, UUID lancamentoId,
                                     UUID movimentoId) throws InterruptedException {
        inicio.await();
        return service.conciliar(tenantId, null, lancamentoId, movimentoId).getMovimentoId();
    }

    private UUID inserirTenant(String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, nome);
        return id;
    }

    private UUID inserirFilial(UUID tenantId, String nome) {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + nome);
        jdbcTemplate.update(
                "INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, nome);
        return filialId;
    }

    private UUID inserirContaFinanceira(UUID tenantId, UUID filialId, String nome) {
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, 'BANCO', 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId, nome);
        return contaId;
    }

    private UUID inserirMovimento(UUID tenantId, UUID filialId, UUID contaId, String descricao) {
        UUID movimentoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras_movimentos
                    (id, tenant_id, filial_id, conta_financeira_id, tipo, valor, descricao, ocorrido_em)
                VALUES (?, ?, ?, ?, 'ENTRADA', 100, ?, CURRENT_TIMESTAMP)
                """, movimentoId, tenantId, filialId, contaId, descricao);
        return movimentoId;
    }

    private UUID inserirLancamentoPendente(UUID tenantId, UUID filialId, UUID contaId) {
        UUID lancamentoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, ocorrido_em, status, criado_em)
                VALUES (?, ?, ?, ?, 'TESTE', ?, 'ENTRADA', 100, 'Conciliacao concorrente',
                        CURRENT_TIMESTAMP, 'PENDENTE', CURRENT_TIMESTAMP)
                """, lancamentoId, tenantId, filialId, contaId, "REF-" + lancamentoId);
        return lancamentoId;
    }
}
