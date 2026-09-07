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
class ContaFinanceiraDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devePermitirContaComUsuarioDoMesmoTenant() {
        UUID tenantId = inserirTenant("Tenant Conta Usuario Valido");
        UUID filialId = inserirFilial(tenantId, "Filial Conta Valida");
        UUID usuarioId = inserirUsuario(tenantId, "Usuario Conta Valido");

        assertDoesNotThrow(() -> inserirContaFinanceira(
                tenantId, filialId, usuarioId, "Conta Usuario Valido"));
    }

    @Test
    void deveRejeitarContaComUsuarioDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Conta Usuario A");
        UUID tenantB = inserirTenant("Tenant Conta Usuario B");
        UUID filialA = inserirFilial(tenantA, "Filial Conta A");
        UUID usuarioB = inserirUsuario(tenantB, "Usuario Conta B");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirContaFinanceira(tenantA, filialA, usuarioB, "Conta Usuario Invalido"));
    }

    @Test
    void devePermitirContaHistoricaSemUsuario() {
        UUID tenantId = inserirTenant("Tenant Conta Historica");
        UUID filialId = inserirFilial(tenantId, "Filial Conta Historica");

        assertDoesNotThrow(() -> inserirContaFinanceira(
                tenantId, filialId, null, "Conta Sem Usuario"));
    }

    @Test
    void devePermitirMovimentoComContaFilialEUsuarioCoerentes() {
        UUID tenantId = inserirTenant("Tenant Movimento Valido");
        UUID filialId = inserirFilial(tenantId, "Filial Movimento Valido");
        UUID usuarioId = inserirUsuario(tenantId, "Usuario Movimento Valido");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, usuarioId, "Conta Movimento Valida");

        assertDoesNotThrow(() -> inserirMovimento(
                tenantId, filialId, contaId, usuarioId));
    }

    @Test
    void deveRejeitarMovimentoComContaDeOutraFilialDoMesmoTenant() {
        UUID tenantId = inserirTenant("Tenant Movimento Filial");
        UUID filialA = inserirFilial(tenantId, "Filial Movimento A");
        UUID filialB = inserirFilial(tenantId, "Filial Movimento B");
        UUID usuarioId = inserirUsuario(tenantId, "Usuario Movimento Filial");
        UUID contaB = inserirContaFinanceira(tenantId, filialB, usuarioId, "Conta Movimento B");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirMovimento(tenantId, filialA, contaB, usuarioId));
    }

    @Test
    void deveRejeitarMovimentoComUsuarioDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Movimento Usuario A");
        UUID tenantB = inserirTenant("Tenant Movimento Usuario B");
        UUID filialA = inserirFilial(tenantA, "Filial Movimento Usuario A");
        UUID usuarioA = inserirUsuario(tenantA, "Usuario Movimento A");
        UUID usuarioB = inserirUsuario(tenantB, "Usuario Movimento B");
        UUID contaA = inserirContaFinanceira(tenantA, filialA, usuarioA, "Conta Movimento Usuario A");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirMovimento(tenantA, filialA, contaA, usuarioB));
    }

    @Test
    void devePermitirMovimentoHistoricoSemUsuario() {
        UUID tenantId = inserirTenant("Tenant Movimento Historico");
        UUID filialId = inserirFilial(tenantId, "Filial Movimento Historico");
        UUID contaId = inserirContaFinanceira(tenantId, filialId, null, "Conta Movimento Historica");

        assertDoesNotThrow(() -> inserirMovimento(
                tenantId, filialId, contaId, null));
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

    private UUID inserirUsuario(UUID tenantId, String nome) {
        UUID usuarioId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash, ativo, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'hash-teste', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, usuarioId, tenantId, nome, usuarioId + "@teste.local");
        return usuarioId;
    }

    private UUID inserirContaFinanceira(UUID tenantId, UUID filialId, UUID usuarioId, String nome) {
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, usuario_id,
                     criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, 'BANCO', 0, TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId, nome, usuarioId);
        return contaId;
    }

    private void inserirMovimento(UUID tenantId, UUID filialId, UUID contaId, UUID usuarioId) {
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras_movimentos
                    (id, tenant_id, filial_id, conta_financeira_id, tipo, valor, descricao,
                     usuario_id, ocorrido_em)
                VALUES (?, ?, ?, ?, 'ENTRADA', 10, 'Movimento teste', ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), tenantId, filialId, contaId, usuarioId);
    }
}
