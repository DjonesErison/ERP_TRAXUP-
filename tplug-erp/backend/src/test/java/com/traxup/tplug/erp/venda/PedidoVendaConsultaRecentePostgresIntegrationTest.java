package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PedidoVendaConsultaRecentePostgresIntegrationTest {

    @Autowired
    private PedidoVendaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveFiltrarPorClienteSemVazarOutroTenantEComParametrosOpcionaisNulos() {
        Fixture a = criarFixture("A");
        Fixture b = criarFixture("B");
        UUID outroClienteA = criarPessoa(a.tenantId(), "Cliente A2");
        Instant instante = Instant.parse("2026-09-07T18:00:00Z");

        UUID esperado = UUID.fromString("00000000-0000-0000-0000-000000000101");
        inserirPedido(esperado, a, a.clienteId(), "A-CLIENTE", instante);
        inserirPedido(UUID.fromString("00000000-0000-0000-0000-000000000102"), a, outroClienteA, "A-OUTRO", instante.plusSeconds(1));
        inserirPedido(UUID.fromString("00000000-0000-0000-0000-000000000103"), b, b.clienteId(), "B-CLIENTE", instante.plusSeconds(2));

        List<PedidoVenda> resultado = repository.buscarRecentesFiltrados(
                a.tenantId(), null, a.clienteId(), null, null, null, PageRequest.of(0, 20));

        assertEquals(1, resultado.size());
        assertEquals(esperado, resultado.getFirst().getId());
        assertEquals(a.tenantId(), resultado.getFirst().getTenantId());
        assertEquals(a.clienteId(), resultado.getFirst().getClienteId());
    }

    @Test
    void clienteNuloDeveTrazerTodosDoTenantSemVazarOutroTenant() {
        Fixture a = criarFixture("NA");
        Fixture b = criarFixture("NB");
        UUID outroClienteA = criarPessoa(a.tenantId(), "Cliente NA2");
        Instant instante = Instant.parse("2026-09-07T19:00:00Z");

        inserirPedido(UUID.randomUUID(), a, a.clienteId(), "NA-1", instante);
        inserirPedido(UUID.randomUUID(), a, outroClienteA, "NA-2", instante.plusSeconds(1));
        inserirPedido(UUID.randomUUID(), b, b.clienteId(), "NB-1", instante.plusSeconds(2));

        List<PedidoVenda> resultado = repository.buscarRecentesFiltrados(
                a.tenantId(), null, null, null, null, null, PageRequest.of(0, 20));

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> a.tenantId().equals(p.getTenantId())));
    }

    @Test
    void deveDesempatarCriadoEmIgualPorIdAscendente() {
        Fixture a = criarFixture("ORD");
        Instant mesmoInstante = Instant.parse("2026-09-07T20:00:00Z");
        UUID menor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID maior = UUID.fromString("00000000-0000-0000-0000-000000000002");

        inserirPedido(maior, a, a.clienteId(), "ORD-2", mesmoInstante);
        inserirPedido(menor, a, a.clienteId(), "ORD-1", mesmoInstante);

        List<PedidoVenda> resultado = repository.buscarRecentesFiltrados(
                a.tenantId(), null, null, null, null, null, PageRequest.of(0, 20));

        assertEquals(List.of(menor, maior), resultado.stream().map(PedidoVenda::getId).toList());
    }

    @Test
    void deveIncluirOsLimitesDoPeriodoEExcluirRegistrosForaDaJanela() {
        Fixture a = criarFixture("PER");
        Instant inicio = Instant.parse("2026-09-07T21:00:00Z");
        Instant fim = Instant.parse("2026-09-07T22:00:00Z");
        UUID noInicio = UUID.fromString("00000000-0000-0000-0000-000000000201");
        UUID noFim = UUID.fromString("00000000-0000-0000-0000-000000000202");

        inserirPedido(UUID.fromString("00000000-0000-0000-0000-000000000200"), a, a.clienteId(), "PER-ANTES", inicio.minusMillis(1));
        inserirPedido(noInicio, a, a.clienteId(), "PER-INICIO", inicio);
        inserirPedido(UUID.fromString("00000000-0000-0000-0000-000000000203"), a, a.clienteId(), "PER-MEIO", inicio.plusSeconds(1800));
        inserirPedido(noFim, a, a.clienteId(), "PER-FIM", fim);
        inserirPedido(UUID.fromString("00000000-0000-0000-0000-000000000204"), a, a.clienteId(), "PER-DEPOIS", fim.plusMillis(1));

        List<PedidoVenda> resultado = repository.buscarRecentesFiltrados(
                a.tenantId(), null, null, null, inicio, fim, PageRequest.of(0, 20));

        assertEquals(List.of(noFim,
                        UUID.fromString("00000000-0000-0000-0000-000000000203"),
                        noInicio),
                resultado.stream().map(PedidoVenda::getId).toList());
    }

    @Test
    void deveCombinarFilialStatusPeriodoERespeitarLimite() {
        Fixture a = criarFixture("COMB");
        UUID outraFilial = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) SELECT ?, tenant_id, empresa_id, ? FROM filiais WHERE id = ?",
                outraFilial, "Filial vendas COMB 2", a.filialId());

        Instant inicio = Instant.parse("2026-09-08T10:00:00Z");
        Instant fim = Instant.parse("2026-09-08T12:00:00Z");
        UUID esperadoMaisRecente = UUID.fromString("00000000-0000-0000-0000-000000000301");
        UUID esperadoSegundo = UUID.fromString("00000000-0000-0000-0000-000000000302");

        inserirPedidoComStatus(esperadoSegundo, a, a.filialId(), a.clienteId(), "COMB-2", "FATURADO", inicio.plusSeconds(1800));
        inserirPedidoComStatus(esperadoMaisRecente, a, a.filialId(), a.clienteId(), "COMB-1", "FATURADO", inicio.plusSeconds(3600));
        inserirPedidoComStatus(UUID.randomUUID(), a, a.filialId(), a.clienteId(), "COMB-RASCUNHO", "RASCUNHO", inicio.plusSeconds(5400));
        inserirPedidoComStatus(UUID.randomUUID(), a, outraFilial, a.clienteId(), "COMB-OUTRA-FILIAL", "FATURADO", inicio.plusSeconds(5400));
        inserirPedidoComStatus(UUID.randomUUID(), a, a.filialId(), a.clienteId(), "COMB-FORA-PERIODO", "FATURADO", fim.plusSeconds(1));

        List<PedidoVenda> resultado = repository.buscarRecentesFiltrados(
                a.tenantId(), a.filialId(), null, "FATURADO", inicio, fim, PageRequest.of(0, 2));

        assertEquals(List.of(esperadoMaisRecente, esperadoSegundo),
                resultado.stream().map(PedidoVenda::getId).toList());
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pessoaId = criarIdsBase(tenantId, empresaId, filialId, sufixo);
        return new Fixture(tenantId, filialId, pessoaId);
    }

    private UUID criarIdsBase(UUID tenantId, UUID empresaId, UUID filialId, String sufixo) {
        UUID pessoaId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant vendas " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa vendas " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial vendas " + sufixo);
        inserirPessoa(pessoaId, tenantId, "Cliente " + sufixo);
        return pessoaId;
    }

    private UUID criarPessoa(UUID tenantId, String nome) {
        UUID pessoaId = UUID.randomUUID();
        inserirPessoa(pessoaId, tenantId, nome);
        return pessoaId;
    }

    private void inserirPessoa(UUID pessoaId, UUID tenantId, String nome) {
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'FISICA', ?, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, nome);
    }

    private void inserirPedido(UUID pedidoId, Fixture fixture, UUID clienteId, String numero, Instant criadoEm) {
        inserirPedidoComStatus(pedidoId, fixture, fixture.filialId(), clienteId, numero, "RASCUNHO", criadoEm);
    }

    private void inserirPedidoComStatus(UUID pedidoId, Fixture fixture, UUID filialId, UUID clienteId,
                                        String numero, String status, Instant criadoEm) {
        jdbcTemplate.update("""
                INSERT INTO pedidos_venda
                    (id, tenant_id, filial_id, cliente_id, numero, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, pedidoId, fixture.tenantId(), filialId, clienteId, numero, status,
                Timestamp.from(criadoEm), Timestamp.from(criadoEm));
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID clienteId) {}
}
