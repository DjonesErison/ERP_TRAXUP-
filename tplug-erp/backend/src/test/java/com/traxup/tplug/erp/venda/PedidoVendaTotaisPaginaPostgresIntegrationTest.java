package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PedidoVendaTotaisPaginaPostgresIntegrationTest {

    @Autowired
    private PedidoVendaItemRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveSomarPedidosDaPaginaSemVazarItensDeOutroTenant() {
        Fixture a = criarFixture("TOT-A");
        Fixture b = criarFixture("TOT-B");
        UUID segundoPedidoA = criarPedido(a.tenantId(), a.filialId(), "TOT-A-2");

        inserirItem(a, a.pedidoId(), new BigDecimal("10.0000"));
        inserirItem(a, a.pedidoId(), new BigDecimal("5.5000"));
        inserirItem(a, segundoPedidoA, new BigDecimal("7.2500"));
        inserirItem(b, b.pedidoId(), new BigDecimal("999.0000"));

        var totais = repository.somarTotaisPorPedidos(
                a.tenantId(), List.of(a.pedidoId(), segundoPedidoA, b.pedidoId()));

        assertEquals(2, totais.size());
        var mapa = totais.stream().collect(java.util.stream.Collectors.toMap(
                PedidoVendaItemRepository.TotalPedido::getPedidoVendaId,
                PedidoVendaItemRepository.TotalPedido::getTotalLiquido));
        assertEquals(0, new BigDecimal("15.5000").compareTo(mapa.get(a.pedidoId())));
        assertEquals(0, new BigDecimal("7.2500").compareTo(mapa.get(segundoPedidoA)));
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

    private UUID criarPedido(UUID tenantId, UUID filialId, String numero) {
        UUID pedidoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pedidos_venda
                    (id, tenant_id, filial_id, numero, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'RASCUNHO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pedidoId, tenantId, filialId, numero);
        return pedidoId;
    }

    private void inserirItem(Fixture fixture, UUID pedidoId, BigDecimal total) {
        jdbcTemplate.update("""
                INSERT INTO pedido_venda_itens
                    (id, tenant_id, pedido_venda_id, produto_id, quantidade, preco_unitario,
                     total_item, desconto_valor, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 1, ?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), fixture.tenantId(), pedidoId, fixture.produtoId(), total, total);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID produtoId, UUID pedidoId) {}
}
