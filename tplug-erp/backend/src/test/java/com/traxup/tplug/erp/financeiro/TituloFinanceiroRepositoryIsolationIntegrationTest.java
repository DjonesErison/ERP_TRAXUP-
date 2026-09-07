package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TituloFinanceiroRepositoryIsolationIntegrationTest {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void filtrosEResumoDeContasReceberDevemRespeitarTenantFilialECliente() {
        Fixture a = criarFixture("A");
        Fixture b = criarFixture("B");

        inserirContaReceber(a, new BigDecimal("100.00"), new BigDecimal("25.00"), "PARCIAL", LocalDate.of(2026, 9, 10));
        inserirContaReceber(a, new BigDecimal("50.00"), new BigDecimal("50.00"), "RECEBIDO", LocalDate.of(2026, 9, 20));
        inserirContaReceber(b, new BigDecimal("999.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 9, 15));

        List<ContaReceber> filtradas = contaReceberRepository.filtrarPorCliente(
                a.tenantId(), a.pessoaId(), a.filialId(), null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertEquals(2, filtradas.size());
        assertEquals(true, filtradas.stream().allMatch(c -> a.tenantId().equals(c.getTenantId())));
        assertEquals(true, filtradas.stream().allMatch(c -> a.filialId().equals(c.getFilialId())));
        assertEquals(true, filtradas.stream().allMatch(c -> a.pessoaId().equals(c.getClienteId())));

        TituloFinanceiroResumoProjection resumo = contaReceberRepository.resumir(
                a.tenantId(), a.filialId(), a.pessoaId(), null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertEquals(2L, resumo.getQuantidade());
        assertEquals(0, new BigDecimal("150.00").compareTo(resumo.getValorOriginalTotal()));
        assertEquals(0, new BigDecimal("75.00").compareTo(resumo.getValorLiquidadoTotal()));
        assertEquals(0, new BigDecimal("75.00").compareTo(resumo.getSaldoAtivoTotal()));
        assertEquals(1L, resumo.getParciais());
        assertEquals(1L, resumo.getLiquidados());
        assertEquals(0L, resumo.getAbertos());
    }

    @Test
    void filtrosEResumoDeContasPagarDevemRespeitarTenantFilialEFornecedor() {
        Fixture a = criarFixture("PA");
        Fixture b = criarFixture("PB");

        inserirContaPagar(a, new BigDecimal("200.00"), new BigDecimal("50.00"), "PARCIAL", LocalDate.of(2026, 10, 5));
        inserirContaPagar(a, new BigDecimal("80.00"), new BigDecimal("80.00"), "PAGO", LocalDate.of(2026, 10, 25));
        inserirContaPagar(b, new BigDecimal("888.00"), BigDecimal.ZERO, "ABERTO", LocalDate.of(2026, 10, 15));

        List<ContaPagar> filtradas = contaPagarRepository.filtrarPorFornecedor(
                a.tenantId(), a.pessoaId(), a.filialId(), null,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals(2, filtradas.size());
        assertEquals(true, filtradas.stream().allMatch(c -> a.tenantId().equals(c.getTenantId())));
        assertEquals(true, filtradas.stream().allMatch(c -> a.filialId().equals(c.getFilialId())));
        assertEquals(true, filtradas.stream().allMatch(c -> a.pessoaId().equals(c.getFornecedorId())));

        TituloFinanceiroResumoProjection resumo = contaPagarRepository.resumir(
                a.tenantId(), a.filialId(), a.pessoaId(), null,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals(2L, resumo.getQuantidade());
        assertEquals(0, new BigDecimal("280.00").compareTo(resumo.getValorOriginalTotal()));
        assertEquals(0, new BigDecimal("130.00").compareTo(resumo.getValorLiquidadoTotal()));
        assertEquals(0, new BigDecimal("150.00").compareTo(resumo.getSaldoAtivoTotal()));
        assertEquals(1L, resumo.getParciais());
        assertEquals(1L, resumo.getLiquidados());
        assertEquals(0L, resumo.getAbertos());
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
                VALUES (?, ?, ?, ?, ?, 'Teste isolamento', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), f.tenantId(), f.filialId(), f.pessoaId(),
                "CR-" + UUID.randomUUID(), original, recebido, vencimento, status);
    }

    private void inserirContaPagar(Fixture f, BigDecimal original, BigDecimal pago,
                                    String status, LocalDate vencimento) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste isolamento', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), f.tenantId(), f.filialId(), f.pessoaId(),
                "CP-" + UUID.randomUUID(), original, pago, vencimento, status);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
