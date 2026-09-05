package com.traxup.tplug.erp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PessoaContatoDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveRejeitarContatoLigadoAPessoaDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant A FK");
        UUID tenantB = inserirTenant("Tenant B FK");
        UUID pessoaA = inserirPessoa(tenantA, "Pessoa A FK");

        assertThrows(DataIntegrityViolationException.class, () -> inserirContato(
                tenantB, pessoaA, "Contato Invalido", "invalido@teste.com", false));
    }

    @Test
    void deveRejeitarDoisContatosPrincipaisParaMesmaPessoa() {
        UUID tenant = inserirTenant("Tenant Principal Unico");
        UUID pessoa = inserirPessoa(tenant, "Pessoa Principal Unico");
        inserirContato(tenant, pessoa, "Principal 1", "principal1@teste.com", true);

        assertThrows(DataIntegrityViolationException.class, () -> inserirContato(
                tenant, pessoa, "Principal 2", "principal2@teste.com", true));
    }

    @Test
    void deveRejeitarContatoSemEmailETelefone() {
        UUID tenant = inserirTenant("Tenant Canal Obrigatorio");
        UUID pessoa = inserirPessoa(tenant, "Pessoa Canal Obrigatorio");

        assertThrows(DataIntegrityViolationException.class, () -> inserirContato(
                tenant, pessoa, "Sem Canal", null, false));
    }

    private UUID inserirTenant(String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, nome);
        return id;
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

    private void inserirContato(UUID tenantId, UUID pessoaId, String nome, String email, boolean principal) {
        jdbcTemplate.update("""
                INSERT INTO pessoa_contatos
                    (id, tenant_id, pessoa_id, nome, email, principal, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), tenantId, pessoaId, nome, email, principal);
    }
}
