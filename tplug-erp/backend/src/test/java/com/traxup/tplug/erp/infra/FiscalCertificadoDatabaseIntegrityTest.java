package com.traxup.tplug.erp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class FiscalCertificadoDatabaseIntegrityTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void rejeitaCertificadoLigadoAFilialDeOutroTenant() {
        UUID tenantA = inserirTenant("Tenant Certificado A");
        UUID tenantB = inserirTenant("Tenant Certificado B");
        UUID filialA = inserirFilial(tenantA, "Filial Certificado A");

        assertThrows(DataIntegrityViolationException.class, () ->
                inserirCertificado(tenantB, filialA, true, "a".repeat(64)));
    }

    @Test
    void permiteSomenteUmCertificadoAtivoPorFilial() {
        UUID tenantId = inserirTenant("Tenant Certificado Ativo");
        UUID filialId = inserirFilial(tenantId, "Filial Certificado Ativo");
        inserirCertificado(tenantId, filialId, true, "b".repeat(64));

        assertThrows(DataIntegrityViolationException.class, () ->
                inserirCertificado(tenantId, filialId, true, "c".repeat(64)));
    }

    @Test
    void permiteHistoricoDeCertificadosInativos() {
        UUID tenantId = inserirTenant("Tenant Historico Certificado");
        UUID filialId = inserirFilial(tenantId, "Filial Historico Certificado");

        assertDoesNotThrow(() -> {
            inserirCertificado(tenantId, filialId, false, "d".repeat(64));
            inserirCertificado(tenantId, filialId, false, "e".repeat(64));
        });
    }

    @Test
    void rejeitaValidadeInvertida() {
        UUID tenantId = inserirTenant("Tenant Validade Certificado");
        UUID filialId = inserirFilial(tenantId, "Filial Validade Certificado");
        OffsetDateTime inicio = OffsetDateTime.now(ZoneOffset.UTC);

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                INSERT INTO fiscal_certificados_digitais (
                    id, tenant_id, filial_id, tipo, titular, documento_titular,
                    numero_serie, thumbprint_sha256, validade_inicio, validade_fim,
                    cofre_segredos, referencia_segredo, ativo)
                VALUES (?, ?, ?, 'A1', 'TRAXUP TESTE', '12345678000199',
                        'SERIE-TESTE', ?, ?, ?, 'EXTERNO', 'certificados/teste', TRUE)
                """, UUID.randomUUID(), tenantId, filialId, "f".repeat(64),
                inicio, inicio.minusDays(1)));
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

    private void inserirCertificado(UUID tenantId, UUID filialId,
                                    boolean ativo, String thumbprint) {
        OffsetDateTime inicio = OffsetDateTime.now(ZoneOffset.UTC);
        jdbc.update("""
                INSERT INTO fiscal_certificados_digitais (
                    id, tenant_id, filial_id, tipo, titular, documento_titular,
                    numero_serie, thumbprint_sha256, validade_inicio, validade_fim,
                    cofre_segredos, referencia_segredo, ativo)
                VALUES (?, ?, ?, 'A1', 'TRAXUP TESTE', '12345678000199',
                        ?, ?, ?, ?, 'EXTERNO', ?, ?)
                """, UUID.randomUUID(), tenantId, filialId, UUID.randomUUID().toString(),
                thumbprint, inicio, inicio.plusYears(1),
                "certificados/" + UUID.randomUUID(), ativo);
    }
}
