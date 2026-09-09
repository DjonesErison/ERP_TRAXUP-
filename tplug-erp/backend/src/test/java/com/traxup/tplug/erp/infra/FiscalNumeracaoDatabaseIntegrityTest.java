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
class FiscalNumeracaoDatabaseIntegrityTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void rejeitaNumeradorLigadoAFilialDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Numeracao A");
        UUID tenantB = inserirTenant("Tenant Numeracao B");
        UUID filialA = inserirFilial(tenantA, "Filial Numeracao A");

        assertThrows(DataIntegrityViolationException.class, () ->
                inserirNumerador(tenantB, filialA, "NFE", "HOMOLOGACAO", 1));
    }

    @Test
    void rejeitaDoisNumeradoresParaMesmoContexto() {
        UUID tenantId = inserirTenant("Tenant Numerador Unico");
        UUID filialId = inserirFilial(tenantId, "Filial Numerador Unico");
        inserirNumerador(tenantId, filialId, "NFCE", "PRODUCAO", 10);

        assertThrows(DataIntegrityViolationException.class, () ->
                inserirNumerador(tenantId, filialId, "NFCE", "PRODUCAO", 10));
    }

    @Test
    void permiteMesmaSerieEmContextosDiferentes() {
        UUID tenantId = inserirTenant("Tenant Series Separadas");
        UUID filialId = inserirFilial(tenantId, "Filial Series Separadas");

        assertDoesNotThrow(() -> {
            inserirNumerador(tenantId, filialId, "NFE", "HOMOLOGACAO", 1);
            inserirNumerador(tenantId, filialId, "NFCE", "HOMOLOGACAO", 1);
            inserirNumerador(tenantId, filialId, "NFE", "PRODUCAO", 1);
        });
    }

    @Test
    void rejeitaSerieForaDoIntervalo() {
        UUID tenantId = inserirTenant("Tenant Serie Invalida");
        UUID filialId = inserirFilial(tenantId, "Filial Serie Invalida");

        assertThrows(DataIntegrityViolationException.class, () ->
                inserirNumerador(tenantId, filialId, "NFE", "HOMOLOGACAO", 0));
    }

    @Test
    void rejeitaNumeroForaDoIntervalo() {
        UUID tenantId = inserirTenant("Tenant Numero Invalido");
        UUID filialId = inserirFilial(tenantId, "Filial Numero Invalido");

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                INSERT INTO fiscal_numeradores
                    (tenant_id, filial_id, modelo, ambiente, serie, ultimo_numero)
                VALUES (?, ?, 'NFE', 'HOMOLOGACAO', 1, 1000000000)
                """, tenantId, filialId));
    }

    private UUID inserirTenant(String nome) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, nome);
        return id;
    }

    private UUID inserirFilial(UUID tenantId, String nome) {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        jdbc.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + nome);
        jdbc.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, nome);
        return filialId;
    }

    private void inserirNumerador(UUID tenantId, UUID filialId, String modelo,
                                                                   String ambiente, int serie) {
        jdbc.update("""
                INSERT INTO fiscal_numeradores
                    (tenant_id, filial_id, modelo, ambiente, serie)
                VALUES (?, ?, ?, ?, ?)
                """, tenantId, filialId, modelo, ambiente, serie);
    }
}
