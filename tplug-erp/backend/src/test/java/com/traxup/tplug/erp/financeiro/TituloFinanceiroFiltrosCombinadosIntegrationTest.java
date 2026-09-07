package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TituloFinanceiroFiltrosCombinadosIntegrationTest {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contaReceberDeveCombinarClienteFilialStatusEPeriodoInclusivo() {
        Fixture alvo = criarFixture("CR-ALVO");
        Fixture outroTenant = criarFixture("CR-OUTRO");

        inserirContaReceber(alvo, new BigDecimal("100.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 11, 1));
        inserirContaReceber(alvo, new BigDecimal("120.00"), new BigDecimal("20.00"), "PARCIAL", LocalDate.of(2026, 11, 15));
        inserirContaReceber(alvo, new BigDecimal("140.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 11, 30));
        inserirContaReceber(outroTenant, new BigDecimal("999.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 11, 15));

        List<ContaReceber> filtradas = contaReceberRepository.filtrarPorCliente(
                alvo.tenantId(), alvo.pessoaId(), alvo.filialId(), "ABERTO",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertEquals(2, filtradas.size());
        assertEquals(LocalDate.of(2026, 11, 1), filtradas.get(0).getVencimento());
        assertEquals(LocalDate.of(2026, 11, 30), filtradas.get(1).getVencimento());
        assertTrue(filtradas.stream().allMatch(c -> "ABERTO".equals(c.getStatus())));
        assertTrue(filtradas.stream().allMatch(c -> alvo.tenantId().equals(c.getTenantId())));

        TituloFinanceiroResumoProjection resumo = contaReceberRepository.resumir(
                alvo.tenantId(), alvo.filialId(), alvo.pessoaId(), "ABERTO",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertEquals(2L, resumo.getQuantidade());
        assertEquals(0, new BigDecimal("240.00").compareTo(resumo.getValorOriginalTotal()));
        assertEquals(2L, resumo.getAbertos());
        assertEquals(0L, resumo.getParciais());
    }

    @Test
    void contaPagarDeveCombinarFornecedorFilialStatusEPeriodoInclusivo() {
        Fixture alvo = criarFixture("CP-ALVO");
        Fixture outroTenant = criarFixture("CP-OUTRO");

        inserirContaPagar(alvo, new BigDecimal("200.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 12, 1));
        inserirContaPagar(alvo, new BigDecimal("220.00"), new BigDecimal("40.00"), "PARCIAL", LocalDate.of(2026, 12, 15));
        inserirContaPagar(alvo, new BigDecimal("240.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 12, 31));
        inserirContaPagar(outroTenant, new BigDecimal("888.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 12, 15));

        List<ContaPagar> filtradas = contaPagarRepository.filtrarPorFornecedor(
                alvo.tenantId(), alvo.pessoaId(), alvo.filialId(), "ABERTO",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

        assertEquals(2, filtradas.size());
        assertEquals(LocalDate.of(2026, 12, 1), filtradas.get(0).getVencimento());
        assertEquals(LocalDate.of(2026, 12, 31), filtradas.get(1).getVencimento());
        assertTrue(filtradas.stream().allMatch(c -> "ABERTO".equals(c.getStatus())));
        assertTrue(filtradas.stream().allMatch(c -> alvo.tenantId().equals(c.getTenantId())));

        TituloFinanceiroResumoProjection resumo = contaPagarRepository.resumir(
                alvo.tenantId(), alvo.filialId(), alvo.pessoaId(), "ABERTO",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

        assertEquals(2L, resumo.getQuantidade());
        assertEquals(0, new BigDecimal("440.00").compareTo(resumo.getValorOriginalTotal()));
        assertEquals(2L, resumo.getAbertos());
        assertEquals(0L, resumo.getParciais());
    }

    @Test
    void resumosSemTitulosDevemRetornarZeros() {
        Fixture fixture = criarFixture("VAZIO");

        TituloFinanceiroResumoProjection receber = contaReceberRepository.resumir(
                fixture.tenantId(), fixture.filialId(), fixture.pessoaId(), null, null, null);
        TituloFinanceiroResumoProjection pagar = contaPagarRepository.resumir(
                fixture.tenantId(), fixture.filialId(), fixture.pessoaId(), null, null, null);

        assertResumoVazio(TituloFinanceiroResumo.deProjection(receber));
        assertResumoVazio(TituloFinanceiroResumo.deProjection(pagar));
    }

    private void assertResumoVazio(TituloFinanceiroResumo resumo) {
        assertEquals(0L, resumo.quantidade());
        assertEquals(0, BigDecimal.ZERO.compareTo(resumo.valorOriginalTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resumo.valorLiquidadoTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resumo.saldoAtivoTotal()));
        assertEquals(0L, resumo.abertos());
        assertEquals(0L, resumo.parciais());
        assertEquals(0L, resumo.liquidados());
        assertEquals(0L, resumo.cancelados());
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

    private void inserirContaReceber(Fixture f, BigDecimal original, BigDecimal recebido,
                                      String status, LocalDate vencimento) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste filtros combinados', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), f.tenantId(), f.filialId(), f.pessoaId(),
                "CR-" + UUID.randomUUID(), original, recebido, vencimento, status);
    }

    private void inserirContaPagar(Fixture f, BigDecimal original, BigDecimal pago,
                                    String status, LocalDate vencimento) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste filtros combinados', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), f.tenantId(), f.filialId(), f.pessoaId(),
                "CP-" + UUID.randomUUID(), original, pago, vencimento, status);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
