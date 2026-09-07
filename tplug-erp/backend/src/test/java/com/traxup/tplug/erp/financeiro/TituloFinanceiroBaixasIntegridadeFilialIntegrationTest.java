package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TituloFinanceiroBaixasIntegridadeFilialIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void recebimentoDeveExigirMesmaFilialDaConta() {
        Fixture fixture = criarFixture("REC-FILIAL");
        UUID contaId = inserirContaReceber(fixture, "CR-FILIAL");

        assertDoesNotThrow(() -> inserirRecebimento(
                fixture.tenantId(), fixture.filialPrincipalId(), contaId, UUID.randomUUID()));

        assertThrows(DataIntegrityViolationException.class, () -> inserirRecebimento(
                fixture.tenantId(), fixture.outraFilialId(), contaId, UUID.randomUUID()));
    }

    @Test
    void pagamentoDeveExigirMesmaFilialDaConta() {
        Fixture fixture = criarFixture("PAG-FILIAL");
        UUID contaId = inserirContaPagar(fixture, "CP-FILIAL");

        assertDoesNotThrow(() -> inserirPagamento(
                fixture.tenantId(), fixture.filialPrincipalId(), contaId, UUID.randomUUID()));

        assertThrows(DataIntegrityViolationException.class, () -> inserirPagamento(
                fixture.tenantId(), fixture.outraFilialId(), contaId, UUID.randomUUID()));
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialPrincipalId = UUID.randomUUID();
        UUID outraFilialId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialPrincipalId, tenantId, empresaId, "Filial principal " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                outraFilialId, tenantId, empresaId, "Outra filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, "Pessoa " + sufixo);

        return new Fixture(tenantId, filialPrincipalId, outraFilialId, pessoaId);
    }

    private UUID inserirContaReceber(Fixture fixture, String documento) {
        UUID id = UUID.randomUUID();
        LocalDateTime criadoEm = LocalDateTime.of(2026, 9, 7, 10, 0);
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Integridade filial recebimento', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, fixture.tenantId(), fixture.filialPrincipalId(), fixture.pessoaId(), documento,
                new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 10, 10), criadoEm, criadoEm);
        return id;
    }

    private UUID inserirContaPagar(Fixture fixture, String documento) {
        UUID id = UUID.randomUUID();
        LocalDateTime criadoEm = LocalDateTime.of(2026, 9, 7, 11, 0);
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Integridade filial pagamento', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, fixture.tenantId(), fixture.filialPrincipalId(), fixture.pessoaId(), documento,
                new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 10, 20), criadoEm, criadoEm);
        return id;
    }

    private void inserirRecebimento(UUID tenantId, UUID filialId, UUID contaId, UUID id) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber_recebimentos
                    (id, tenant_id, filial_id, conta_receber_id, valor, recebido_em)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, tenantId, filialId, contaId, new BigDecimal("10.00"),
                OffsetDateTime.of(2026, 9, 7, 14, 0, 0, 0, ZoneOffset.UTC));
    }

    private void inserirPagamento(UUID tenantId, UUID filialId, UUID contaId, UUID id) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar_pagamentos
                    (id, tenant_id, filial_id, conta_pagar_id, valor, pago_em)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, tenantId, filialId, contaId, new BigDecimal("10.00"),
                OffsetDateTime.of(2026, 9, 7, 15, 0, 0, 0, ZoneOffset.UTC));
    }

    private record Fixture(UUID tenantId, UUID filialPrincipalId, UUID outraFilialId, UUID pessoaId) {}
}
