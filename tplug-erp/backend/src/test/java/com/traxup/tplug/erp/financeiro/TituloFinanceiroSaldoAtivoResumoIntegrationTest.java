package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class TituloFinanceiroSaldoAtivoResumoIntegrationTest {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void resumoReceberDeveSomarSaldoAtivoSomenteDeAbertoEParcial() {
        Fixture fixture = criarFixture("CR-SALDO");

        inserirContaReceber(fixture, new BigDecimal("100.00"), BigDecimal.ZERO, "ABERTO");
        inserirContaReceber(fixture, new BigDecimal("200.00"), new BigDecimal("50.00"), "PARCIAL");
        inserirContaReceber(fixture, new BigDecimal("300.00"), new BigDecimal("300.00"), "RECEBIDO");
        inserirContaReceber(fixture, new BigDecimal("400.00"), BigDecimal.ZERO, "CANCELADO");

        TituloFinanceiroResumo resumo = TituloFinanceiroResumo.deProjection(contaReceberRepository.resumir(
                fixture.tenantId(), fixture.filialId(), fixture.pessoaId(), null, null, null));

        assertEquals(4L, resumo.quantidade());
        assertValor("1000.00", resumo.valorOriginalTotal());
        assertValor("350.00", resumo.valorLiquidadoTotal());
        assertValor("250.00", resumo.saldoAtivoTotal());
        assertEquals(1L, resumo.abertos());
        assertEquals(1L, resumo.parciais());
        assertEquals(1L, resumo.liquidados());
        assertEquals(1L, resumo.cancelados());
    }

    @Test
    void resumoPagarDeveSomarSaldoAtivoSomenteDeAbertoEParcial() {
        Fixture fixture = criarFixture("CP-SALDO");

        inserirContaPagar(fixture, new BigDecimal("120.00"), BigDecimal.ZERO, "ABERTO");
        inserirContaPagar(fixture, new BigDecimal("220.00"), new BigDecimal("20.00"), "PARCIAL");
        inserirContaPagar(fixture, new BigDecimal("320.00"), new BigDecimal("320.00"), "PAGO");
        inserirContaPagar(fixture, new BigDecimal("420.00"), BigDecimal.ZERO, "CANCELADO");

        TituloFinanceiroResumo resumo = TituloFinanceiroResumo.deProjection(contaPagarRepository.resumir(
                fixture.tenantId(), fixture.filialId(), fixture.pessoaId(), null, null, null));

        assertEquals(4L, resumo.quantidade());
        assertValor("1080.00", resumo.valorOriginalTotal());
        assertValor("340.00", resumo.valorLiquidadoTotal());
        assertValor("320.00", resumo.saldoAtivoTotal());
        assertEquals(1L, resumo.abertos());
        assertEquals(1L, resumo.parciais());
        assertEquals(1L, resumo.liquidados());
        assertEquals(1L, resumo.cancelados());
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual));
    }

    private Fixture criarFixture(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial " + sufixo);
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, "Pessoa " + sufixo);

        return new Fixture(tenantId, filialId, pessoaId);
    }

    private void inserirContaReceber(Fixture f, BigDecimal original, BigDecimal recebido, String status) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste saldo ativo resumo', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), f.tenantId(), f.filialId(), f.pessoaId(),
                "CR-" + UUID.randomUUID(), original, recebido, LocalDate.of(2026, 12, 20), status);
    }

    private void inserirContaPagar(Fixture f, BigDecimal original, BigDecimal pago, String status) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste saldo ativo resumo', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), f.tenantId(), f.filialId(), f.pessoaId(),
                "CP-" + UUID.randomUUID(), original, pago, LocalDate.of(2026, 12, 20), status);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
