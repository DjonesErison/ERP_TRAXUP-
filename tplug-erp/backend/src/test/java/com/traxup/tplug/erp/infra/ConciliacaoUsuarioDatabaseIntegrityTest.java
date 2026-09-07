package com.traxup.tplug.erp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ConciliacaoUsuarioDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveRejeitarUsuarioDeOutroTenantNaConciliacao() {
        UUID tenantA = inserirTenant("Tenant Conciliacao Usuario A");
        UUID tenantB = inserirTenant("Tenant Conciliacao Usuario B");
        UUID filialA = inserirFilial(tenantA, "Filial A");
        UUID contaA = inserirContaFinanceira(tenantA, filialA, "Conta A");
        UUID usuarioB = inserirUsuario(tenantB, "Usuario B");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirConciliacao(tenantA, filialA, contaA, usuarioB, "REF-USUARIO-CROSS-TENANT"));
    }

    @Test
    void devePermitirConciliacaoHistoricaSemUsuario() {
        UUID tenantId = inserirTenant("Tenant Conciliacao Historica");
        UUID filialId = inserirFilial(tenantId, "Filial Historica");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Historica");

        assertDoesNotThrow(() -> inserirConciliacao(tenantId, filialId, contaId, null, "REF-SEM-USUARIO"));
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
                VALUES (?, ?, ?, ?, 'BANCO', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId, nome);
        return contaId;
    }

    private UUID inserirUsuario(UUID tenantId, String nome) {
        UUID usuarioId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash, ativo, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'hash-teste', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, usuarioId, tenantId, nome, usuarioId + "@teste.local");
        return usuarioId;
    }

    private void inserirConciliacao(UUID tenantId, UUID filialId, UUID contaId, UUID usuarioId, String referencia) {
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, natureza, ocorrido_em, status, usuario_id, criado_em)
                VALUES (?, ?, ?, ?, 'TESTE', ?, 'ENTRADA', 100, 'Conciliacao teste', 'NORMAL',
                        CURRENT_TIMESTAMP, 'PENDENTE', ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), tenantId, filialId, contaId, referencia, usuarioId);
    }
}
