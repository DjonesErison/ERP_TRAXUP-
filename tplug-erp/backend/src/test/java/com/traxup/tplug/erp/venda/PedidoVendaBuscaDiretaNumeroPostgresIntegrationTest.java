package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PedidoVendaBuscaDiretaNumeroPostgresIntegrationTest {
    @Autowired PedidoVendaRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void deveBuscarNumeroSemDiferenciarCaixaESemVazarOutroTenant() {
        Fixture a = criarFixture("DIR-A");
        Fixture b = criarFixture("DIR-B");
        UUID esperado = UUID.randomUUID();
        Instant instante = Instant.parse("2026-09-08T02:00:00Z");

        inserirPedido(esperado, a, "PV-DIRETO", instante);
        inserirPedido(UUID.randomUUID(), b, "PV-DIRETO", instante.plusSeconds(1));

        var resultado = repository.findByTenantIdAndNumeroIgnoreCase(a.tenantId(), "pv-direto");

        assertTrue(resultado.isPresent());
        assertEquals(esperado, resultado.orElseThrow().getId());
        assertEquals(a.tenantId(), resultado.orElseThrow().getTenantId());
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)", empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)", filialId, tenantId, empresaId, "Filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'FISICA', ?, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, clienteId, tenantId, "Cliente " + sufixo);
        return new Fixture(tenantId, filialId, clienteId);
    }

    private void inserirPedido(UUID pedidoId, Fixture fixture, String numero, Instant criadoEm) {
        jdbcTemplate.update("""
                INSERT INTO pedidos_venda
                    (id, tenant_id, filial_id, cliente_id, numero, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, 'RASCUNHO', ?, ?)
                """, pedidoId, fixture.tenantId(), fixture.filialId(), fixture.clienteId(), numero,
                Timestamp.from(criadoEm), Timestamp.from(criadoEm));
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID clienteId) {}
}
