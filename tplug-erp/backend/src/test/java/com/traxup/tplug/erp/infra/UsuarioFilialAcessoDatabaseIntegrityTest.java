package com.traxup.tplug.erp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UsuarioFilialAcessoDatabaseIntegrityTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @Transactional
    void aceitaFilialDoTenantERejeitaFilialDeOutroTenant() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        UUID filialA = UUID.randomUUID();
        UUID filialB = UUID.randomUUID();

        inserirTenant(tenantA, "Tenant A acesso filial");
        inserirTenant(tenantB, "Tenant B acesso filial");
        inserirEmpresa(empresaA, tenantA, "Empresa A acesso filial");
        inserirEmpresa(empresaB, tenantB, "Empresa B acesso filial");
        inserirFilial(filialA, tenantA, empresaA, "Filial A");
        inserirFilial(filialB, tenantB, empresaB, "Filial B");
        jdbc.update("""
                INSERT INTO usuarios
                    (id, tenant_id, nome, email, senha_hash)
                VALUES (?, ?, 'Usuario A', ?, 'hash-teste')
                """, usuarioA, tenantA, usuarioA + "@teste.local");

        jdbc.update("""
                INSERT INTO usuario_filiais
                    (tenant_id, usuario_id, filial_id)
                VALUES (?, ?, ?)
                """, tenantA, usuarioA, filialA);
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*) FROM usuario_filiais
                WHERE tenant_id = ? AND usuario_id = ?
                """, Integer.class, tenantA, usuarioA));

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        INSERT INTO usuario_filiais
                            (tenant_id, usuario_id, filial_id)
                        VALUES (?, ?, ?)
                        """, tenantA, usuarioA, filialB));
    }

    private void inserirTenant(UUID id, String nome) {
        jdbc.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, nome);
    }

    private void inserirEmpresa(UUID id, UUID tenantId, String nome) {
        jdbc.update("""
                INSERT INTO empresas (id, tenant_id, razao_social)
                VALUES (?, ?, ?)
                """, id, tenantId, nome);
    }

    private void inserirFilial(UUID id, UUID tenantId,
                               UUID empresaId, String nome) {
        jdbc.update("""
                INSERT INTO filiais (id, tenant_id, empresa_id, nome)
                VALUES (?, ?, ?, ?)
                """, id, tenantId, empresaId, nome);
    }
}
