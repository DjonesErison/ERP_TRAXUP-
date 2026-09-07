package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PedidoVendaItemOrdenacaoPostgresIntegrationTest {

    @Autowired
    private PedidoVendaItemRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveOrdenarPorCriadoEmEIdSemVazarOutroTenantOuPedido() {
        Fixture a = criarFixture("IT-A");
        Fixture b = criarFixture("IT-B");
        UUID outroPedidoA = criarPedido(a, "IT-A-2");
        Instant primeiroInstante = Instant.parse("2026-09-08T13:00:00Z");
        Instant mesmoInstante = Instant.parse("2026-09-08T14:00:00Z");

        UUID primeiro = UUID.fromString("00000000-0000-0000-0000-000000000401");
        UUID menorNoEmpate = UUID.fromString("00000000-0000-0000-0000-000000000402");
        UUID maiorNoEmpate = UUID.fromString("00000000-0000-0000-0000-000000000403");

        inserirItem(maiorNoEmpate, a, a.pedidoId(), mesmoInstante);
        inserirItem(primeiro, a, a.pedidoId(), primeiroInstante);
        inserirItem(menorNoEmpate, a, a.pedidoId(), mesmoInstante);
        inserirItem(UUID.randomUUID(), a, outroPedidoA, primeiroInstante.minusSeconds(1));
        inserirItem(UUID.randomUUID(), b, b.pedidoId(), primeiroInstante.minusSeconds(1));

        List<PedidoVendaItem> resultado = repository
                .findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(a.tenantId(), a.pedidoId());

        assertEquals(List.of(primeiro, menorNoEmpate, maiorNoEmpate),
                resultado.stream().map(PedidoVendaItem::getId).toList());
        assertTrue(resultado.stream().allMatch(i -> a.tenantId().equals(i.getTenantId())));
        assertTrue(resultado.stream().allMatch(i -> a.pedidoId().equals(i.getPedidoVendaId())));
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO produtos
                    (id, tenant_id, codigo, descricao, venda_prc, compra_prc, unidade, ativo, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 10, 5, 'UN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, produtoId, tenantId, "PROD-" + sufixo, "Produto " + sufixo);

        UUID pedidoId = criarPedido(tenantId, filialId, sufixo + "-1");
        return new Fixture(tenantId, filialId, produtoId, pedidoId);
    }

    private UUID criarPedido(Fixture fixture, String numero) {
        return criarPedido(fixture.tenantId(), fixture.filialId(), numero);
    }

    private UUID criarPedido(UUID tenantId, UUID filialId, String numero) {
        UUID pedidoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pedidos_venda
                    (id, tenant_id, filial_id, numero, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'RASCUNHO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pedidoId, tenantId, filialId, numero);
        return pedidoId;
    }

    private void inserirItem(UUID itemId, Fixture fixture, UUID pedidoId, Instant criadoEm) {
        jdbcTemplate.update("""
                INSERT INTO pedido_venda_itens
                    (id, tenant_id, pedido_venda_id, produto_id, quantidade, preco_unitario,
                     total_item, desconto_valor, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 1, 10, 10, 0, ?, ?)
                """, itemId, fixture.tenantId(), pedidoId, fixture.produtoId(),
                Timestamp.from(criadoEm), Timestamp.from(criadoEm));
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID produtoId, UUID pedidoId) {}
}
