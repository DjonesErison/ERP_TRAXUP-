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
class MultiTenantDatabaseIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void deveRejeitarFilialLigadaAEmpresaDeOutroTenant() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID empresaDoTenantA = UUID.randomUUID();
        UUID filialDoTenantB = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO tenants (id, nome) VALUES (?, ?)",
                tenantA,
                "Tenant A"
        );

        jdbcTemplate.update(
                "INSERT INTO tenants (id, nome) VALUES (?, ?)",
                tenantB,
                "Tenant B"
        );

        jdbcTemplate.update(
                "INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaDoTenantA,
                tenantA,
                "Empresa Tenant A"
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                        filialDoTenantB,
                        tenantB,
                        empresaDoTenantA,
                        "Filial inválida"
                )
        );
    }
}
