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
}
