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
class IntegracaoFinanceiraDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devePermitirContaFilialEUsuarioDoMesmoTenant() {
        UUID tenantId = inserirTenant("Tenant Integracao Valida");
        UUID filialId = inserirFilial(tenantId, "Filial Valida");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Valida");
        UUID usuarioId = inserirUsuario(tenantId, "Usuario Valido");

        assertDoesNotThrow(() -> inserirIntegracao(
                tenantId, filialId, contaId, usuarioId, "PROVEDOR_VALIDO"));
    }

    @Test
    void deveRejeitarContaDeOutraFilialDoMesmoTenant() {
        UUID tenantId = inserirTenant("Tenant Integracao Filial");
        UUID filialA = inserirFilial(tenantId, "Filial A");
        UUID filialB = inserirFilial(tenantId, "Filial B");
        UUID contaB = inserirContaFinanceira(tenantId, filialB, "Conta Filial B");
        UUID usuarioId = inserirUsuario(tenantId, "Usuario Filial");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirIntegracao(tenantId, filialA, contaB, usuarioId, "PROVEDOR_FILIAL_INVALIDA"));
    }

    @Test
    void deveRejeitarUsuarioDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Integracao Usuario A");
        UUID tenantB = inserirTenant("Tenant Integracao Usuario B");
        UUID filialA = inserirFilial(tenantA, "Filial Usuario A");
        UUID contaA = inserirContaFinanceira(tenantA, filialA, "Conta Usuario A");
        UUID usuarioB = inserirUsuario(tenantB, "Usuario B");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirIntegracao(tenantA, filialA, contaA, usuarioB, "PROVEDOR_USUARIO_INVALIDO"));
    }

    @Test
    void devePermitirIntegracaoHistoricaSemUsuario() {
        UUID tenantId = inserirTenant("Tenant Integracao Historica");
        UUID filialId = inserirFilial(tenantId, "Filial Historica");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, "Conta Historica");

        assertDoesNotThrow(() -> inserirIntegracao(
                tenantId, filialId, contaId, null, "PROVEDOR_SEM_USUARIO"));
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

    private void inserirIntegracao(UUID tenantId, UUID filialId, UUID contaId, UUID usuarioId, String provedor) {
        jdbcTemplate.update("""
                INSERT INTO integracoes_financeiras
                    (id, tenant_id, filial_id, conta_financeira_id, provedor, identificador_externo,
                     ativo, usuario_id, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, 'ID-TESTE', TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), tenantId, filialId, contaId, provedor, usuarioId);
    }
}
