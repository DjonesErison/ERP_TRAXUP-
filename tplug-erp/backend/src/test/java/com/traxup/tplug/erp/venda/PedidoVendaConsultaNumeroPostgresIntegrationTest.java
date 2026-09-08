package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PedidoVendaConsultaNumeroPostgresIntegrationTest {

    @Autowired
    private PedidoVendaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveFiltrarNumeroSemDiferenciarCaixaESemVazarOutroTenant() {
        Fixture a = criarFixture("NUM-A");
        Fixture b = criarFixture("NUM-B");
        Instant instante = Instant.parse("2026-09-08T01:00:00Z");
        UUID esperado = UUID.randomUUID();

        inserirPedido(esperado, a, "PV-123", instante);
        inserirPedido(UUID.randomUUID(), a, "PV-999", instante.plusSeconds(1));
        inserirPedido(UUID.randomUUID(), b, "PV-123", instante.plusSeconds(2));

        var resultado = repository.buscarRecentesFiltradosPaginado(
                a.tenantId(), null, null, "pv-123", null, null, null, PageRequest.of(0, 20));

        assertEquals(1, resultado.getTotalElements());
        assertEquals(esperado, resultado.getContent().getFirst().getId());
        assertEquals(a.tenantId(), resultado.getContent().getFirst().getTenantId());
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)", empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)", filialId, tenantId, empresaId, "Filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'FISICA', ?, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, "Cliente " + sufixo);
        return new Fixture(tenantId, filialId, pessoaId);
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
