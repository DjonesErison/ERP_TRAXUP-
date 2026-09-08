package com.traxup.tplug.erp.produto.combo;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ProdutoComboPostgresIntegrationTest {
    @Autowired ProdutoComboApplicationService service;
    @Autowired ProdutoComboComponenteRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void deveConfigurarESubstituirComposicaoDoCombo() {
        Fixture fixture = criarFixture("CFG", 3);
        UUID comboId = fixture.produtos().get(0);
        UUID primeiro = fixture.produtos().get(1);
        UUID segundo = fixture.produtos().get(2);

        var configurado = service.configurar(fixture.tenantId(), comboId, List.of(
                new ProdutoComboApplicationService.ComponenteConfig(primeiro, new BigDecimal("2.0000")),
                new ProdutoComboApplicationService.ComponenteConfig(segundo, new BigDecimal("1.5000"))));

        assertEquals(2, configurado.size());

        service.configurar(fixture.tenantId(), comboId, List.of(
                new ProdutoComboApplicationService.ComponenteConfig(segundo, new BigDecimal("3.0000"))));

        var persistidos = repository.findAllByTenantIdAndComboProdutoIdOrderByComponenteProdutoIdAsc(
                fixture.tenantId(), comboId);
        assertEquals(1, persistidos.size());
        assertEquals(segundo, persistidos.getFirst().getComponenteProdutoId());
        assertEquals(0, new BigDecimal("3.0000").compareTo(persistidos.getFirst().getQuantidade()));
    }

    @Test
    void naoDeveAceitarComponenteDeOutroTenant() {
        Fixture a = criarFixture("TEN-A", 1);
        Fixture b = criarFixture("TEN-B", 1);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.configurar(
                a.tenantId(), a.produtos().getFirst(), List.of(
                        new ProdutoComboApplicationService.ComponenteConfig(
                                b.produtos().getFirst(), BigDecimal.ONE))));

        assertEquals(0, repository.findAllByTenantIdAndComboProdutoIdOrderByComponenteProdutoIdAsc(
                a.tenantId(), a.produtos().getFirst()).size());
    }

    @Test
    void naoDevePermitirComboDentroDeCombo() {
        Fixture fixture = criarFixture("NEST", 3);
        UUID comboPrincipal = fixture.produtos().get(0);
        UUID comboComponente = fixture.produtos().get(1);
        UUID produtoDireto = fixture.produtos().get(2);

        service.configurar(fixture.tenantId(), comboComponente, List.of(
                new ProdutoComboApplicationService.ComponenteConfig(produtoDireto, BigDecimal.ONE)));

        assertThrows(IllegalArgumentException.class, () -> service.configurar(
                fixture.tenantId(), comboPrincipal, List.of(
                        new ProdutoComboApplicationService.ComponenteConfig(comboComponente, BigDecimal.ONE))));
    }

    private Fixture criarFixture(String sufixo, int quantidadeProdutos) {
        UUID tenantId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);

        java.util.ArrayList<UUID> produtos = new java.util.ArrayList<>();
        for (int i = 1; i <= quantidadeProdutos; i++) {
            UUID produtoId = UUID.randomUUID();
            jdbcTemplate.update("""
                    INSERT INTO produtos
                        (id, tenant_id, codigo, descricao, venda_prc, compra_prc, unidade, ativo, criado_em, atualizado_em)
                    VALUES (?, ?, ?, ?, 10, 5, 'UN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, produtoId, tenantId, "COMBO-" + sufixo + "-" + i, "Produto " + sufixo + " " + i);
            produtos.add(produtoId);
        }
        return new Fixture(tenantId, List.copyOf(produtos));
    }

    private record Fixture(UUID tenantId, List<UUID> produtos) {}
}
