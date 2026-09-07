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
class TitulosFinanceirosUsuarioDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devePermitirUsuarioDoMesmoTenantEmContaReceber() {
        Contexto contexto = criarContexto("Receber Valido");
        UUID usuarioId = inserirUsuario(contexto.tenantId(), "Usuario Receber");

        assertDoesNotThrow(() -> inserirContaReceber(contexto, usuarioId));
    }

    @Test
    void deveRejeitarUsuarioDeOutroTenantEmContaReceber() {
        Contexto contexto = criarContexto("Receber Invalido");
        UUID outroTenant = inserirTenant("Outro Tenant Receber");
        UUID usuarioOutroTenant = inserirUsuario(outroTenant, "Usuario Outro Receber");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirContaReceber(contexto, usuarioOutroTenant));
    }

    @Test
    void devePermitirContaReceberHistoricaSemUsuario() {
        Contexto contexto = criarContexto("Receber Historico");

        assertDoesNotThrow(() -> inserirContaReceber(contexto, null));
    }

    @Test
    void devePermitirUsuarioDoMesmoTenantEmContaPagar() {
        Contexto contexto = criarContexto("Pagar Valido");
        UUID usuarioId = inserirUsuario(contexto.tenantId(), "Usuario Pagar");

        assertDoesNotThrow(() -> inserirContaPagar(contexto, usuarioId));
    }

    @Test
    void deveRejeitarUsuarioDeOutroTenantEmContaPagar() {
        Contexto contexto = criarContexto("Pagar Invalido");
        UUID outroTenant = inserirTenant("Outro Tenant Pagar");
        UUID usuarioOutroTenant = inserirUsuario(outroTenant, "Usuario Outro Pagar");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirContaPagar(contexto, usuarioOutroTenant));
    }

    @Test
    void devePermitirContaPagarHistoricaSemUsuario() {
        Contexto contexto = criarContexto("Pagar Historico");

        assertDoesNotThrow(() -> inserirContaPagar(contexto, null));
    }

    private Contexto criarContexto(String sufixo) {
        UUID tenantId = inserirTenant("Tenant " + sufixo);
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update(
                "INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, "Pessoa " + sufixo);

        return new Contexto(tenantId, filialId, pessoaId);
    }

    private UUID inserirTenant(String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, nome);
        return id;
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

    private void inserirContaReceber(Contexto contexto, UUID usuarioId) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, usuario_id, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, 'Titulo receber teste', 100, 0, CURRENT_DATE, 'ABERTO', ?,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), contexto.tenantId(), contexto.filialId(), contexto.pessoaId(),
                UUID.randomUUID().toString(), usuarioId);
    }

    private void inserirContaPagar(Contexto contexto, UUID usuarioId) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, usuario_id, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Titulo pagar teste', 100, 0, CURRENT_DATE, 'ABERTO', ?,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), contexto.tenantId(), contexto.filialId(), contexto.pessoaId(),
                UUID.randomUUID().toString(), usuarioId);
    }

    private record Contexto(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
