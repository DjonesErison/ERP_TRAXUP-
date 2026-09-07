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
class TituloFinanceiroBaixasIntegridadeUsuarioIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void recebimentoDeveAceitarUsuarioDoMesmoTenantERejeitarUsuarioDeOutroTenant() {
        Fixture fixture = criarFixture("REC-USUARIO");
        UUID contaId = inserirContaReceber(fixture, "CR-USUARIO");

        assertDoesNotThrow(() -> inserirRecebimento(
                fixture.tenantId(), fixture.filialId(), contaId, fixture.usuarioMesmoTenantId(), UUID.randomUUID()));

        assertThrows(DataIntegrityViolationException.class, () -> inserirRecebimento(
                fixture.tenantId(), fixture.filialId(), contaId, fixture.usuarioOutroTenantId(), UUID.randomUUID()));
    }

    @Test
    void pagamentoDeveAceitarUsuarioDoMesmoTenantERejeitarUsuarioDeOutroTenant() {
        Fixture fixture = criarFixture("PAG-USUARIO");
        UUID contaId = inserirContaPagar(fixture, "CP-USUARIO");

        assertDoesNotThrow(() -> inserirPagamento(
                fixture.tenantId(), fixture.filialId(), contaId, fixture.usuarioMesmoTenantId(), UUID.randomUUID()));

        assertThrows(DataIntegrityViolationException.class, () -> inserirPagamento(
                fixture.tenantId(), fixture.filialId(), contaId, fixture.usuarioOutroTenantId(), UUID.randomUUID()));
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID outroTenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID usuarioMesmoTenantId = UUID.randomUUID();
        UUID usuarioOutroTenantId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", outroTenantId, "Outro tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, "Pessoa " + sufixo);
        inserirUsuario(usuarioMesmoTenantId, tenantId, "mesmo-" + sufixo.toLowerCase() + "@traxup.test");
        inserirUsuario(usuarioOutroTenantId, outroTenantId, "outro-" + sufixo.toLowerCase() + "@traxup.test");

        return new Fixture(tenantId, filialId, pessoaId, usuarioMesmoTenantId, usuarioOutroTenantId);
    }

    private void inserirUsuario(UUID usuarioId, UUID tenantId, String email) {
        jdbcTemplate.update("""
                INSERT INTO usuarios (id, tenant_id, nome, email, senha_hash, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'Usuario teste', ?, 'hash-teste', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, usuarioId, tenantId, email);
    }

    private UUID inserirContaReceber(Fixture fixture, String documento) {
        UUID id = UUID.randomUUID();
        LocalDateTime criadoEm = LocalDateTime.of(2026, 9, 7, 10, 0);
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Integridade usuario recebimento', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, fixture.tenantId(), fixture.filialId(), fixture.pessoaId(), documento,
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
                VALUES (?, ?, ?, ?, ?, 'Integridade usuario pagamento', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, fixture.tenantId(), fixture.filialId(), fixture.pessoaId(), documento,
                new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 10, 20), criadoEm, criadoEm);
        return id;
    }

    private void inserirRecebimento(UUID tenantId, UUID filialId, UUID contaId, UUID usuarioId, UUID id) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber_recebimentos
                    (id, tenant_id, filial_id, conta_receber_id, valor, usuario_id, recebido_em)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, tenantId, filialId, contaId, new BigDecimal("10.00"), usuarioId,
                OffsetDateTime.of(2026, 9, 7, 14, 0, 0, 0, ZoneOffset.UTC));
    }

    private void inserirPagamento(UUID tenantId, UUID filialId, UUID contaId, UUID usuarioId, UUID id) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar_pagamentos
                    (id, tenant_id, filial_id, conta_pagar_id, valor, usuario_id, pago_em)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, tenantId, filialId, contaId, new BigDecimal("10.00"), usuarioId,
                OffsetDateTime.of(2026, 9, 7, 15, 0, 0, 0, ZoneOffset.UTC));
    }

    private record Fixture(
            UUID tenantId,
            UUID filialId,
            UUID pessoaId,
            UUID usuarioMesmoTenantId,
            UUID usuarioOutroTenantId) {}
}
