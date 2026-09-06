package com.traxup.tplug.erp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class FinanceiroDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveRejeitarContaComFilialDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Financeiro A");
        UUID tenantB = inserirTenant("Tenant Financeiro B");
        UUID filialA = inserirFilial(tenantA, "Filial A");
        UUID clienteB = inserirPessoa(tenantB, "Cliente B");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirConta(tenantB, filialA, clienteB));
    }

    @Test
    void deveRejeitarContaComClienteDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Cliente A");
        UUID tenantB = inserirTenant("Tenant Cliente B");
        UUID filialB = inserirFilial(tenantB, "Filial B");
        UUID clienteA = inserirPessoa(tenantA, "Cliente A");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirConta(tenantB, filialB, clienteA));
    }

    @Test
    void deveRejeitarConciliacaoComMovimentoDeOutraConta() {
        UUID tenantId = inserirTenant("Tenant Conciliacao Contexto");
        UUID filialId = inserirFilial(tenantId, "Filial Conciliacao Contexto");
        UUID contaA = inserirContaFinanceira(tenantId, filialId, "Conta A");
        UUID contaB = inserirContaFinanceira(tenantId, filialId, "Conta B");
        UUID movimentoB = inserirMovimento(tenantId, filialId, contaB);

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirConciliacao(tenantId, filialId, contaA, movimentoB,
                        "REF-CONTEXTO", "CONCILIADO", true));
    }

    @Test
    void deveRejeitarReutilizacaoDoMesmoMovimento() {
        UUID tenantId = inserirTenant("Tenant Conciliacao Unica");
        UUID filialId = inserirFilial(tenantId, "Filial Conciliacao Unica");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Unica");
        UUID movimentoId = inserirMovimento(tenantId, filialId, contaId);
        inserirConciliacao(tenantId, filialId, contaId, movimentoId,
                "REF-UNICA-1", "CONCILIADO", true);

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirConciliacao(tenantId, filialId, contaId, movimentoId,
                        "REF-UNICA-2", "CONCILIADO", true));
    }

    @Test
    void deveRejeitarEstadoPendenteComDataDeConciliacao() {
        UUID tenantId = inserirTenant("Tenant Estado Conciliacao");
        UUID filialId = inserirFilial(tenantId, "Filial Estado Conciliacao");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Estado");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirConciliacao(tenantId, filialId, contaId, null,
                        "REF-ESTADO", "PENDENTE", true));
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

    private UUID inserirPessoa(UUID tenantId, String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor,
                     ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, tenantId, nome);
        return id;
    }

    private void inserirConta(UUID tenantId, UUID filialId, UUID clienteId) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'DOC-TESTE', 'Conta teste', ?, 0, ?, 'ABERTO',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), tenantId, filialId, clienteId,
                new BigDecimal("100.00"), LocalDate.now().plusDays(30));
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

    private UUID inserirMovimento(UUID tenantId, UUID filialId, UUID contaId) {
        UUID movimentoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras_movimentos
                    (id, tenant_id, filial_id, conta_financeira_id, tipo, valor, descricao, ocorrido_em)
                VALUES (?, ?, ?, ?, 'ENTRADA', 100, 'Movimento teste', CURRENT_TIMESTAMP)
                """, movimentoId, tenantId, filialId, contaId);
        return movimentoId;
    }

    private void inserirConciliacao(UUID tenantId, UUID filialId, UUID contaId, UUID movimentoId,
                                    String referencia, String status, boolean comDataConciliacao) {
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, ocorrido_em, status, movimento_id, criado_em, conciliado_em)
                VALUES (?, ?, ?, ?, 'TESTE', ?, 'ENTRADA', 100, 'Conciliacao teste',
                        CURRENT_TIMESTAMP, ?, ?, CURRENT_TIMESTAMP,
                        CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END)
                """, UUID.randomUUID(), tenantId, filialId, contaId, referencia,
                status, movimentoId, comDataConciliacao);
    }
}
