package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class TituloFinanceiroBaixasOrdenacaoIntegrationTest {

    @Autowired
    private ContaReceberRecebimentoRepository recebimentoRepository;

    @Autowired
    private ContaPagarPagamentoRepository pagamentoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void recebimentosDevemDesempatarPorIdEPreservarTenant() {
        Fixture principal = criarFixture("REC-HIST");
        Fixture outroTenant = criarFixture("REC-OUTRO");
        UUID contaId = inserirContaReceber(principal, "CR-HIST");
        UUID contaOutroTenant = inserirContaReceber(outroTenant, "CR-OUTRO");
        Instant instante = Instant.parse("2026-09-07T12:00:00Z");
        UUID idMenor = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID idMaior = UUID.fromString("00000000-0000-0000-0000-000000000102");

        inserirRecebimento(principal, contaId, idMaior, instante);
        inserirRecebimento(principal, contaId, idMenor, instante);
        inserirRecebimento(outroTenant, contaOutroTenant,
                UUID.fromString("00000000-0000-0000-0000-000000000100"), instante.plusSeconds(60));

        List<ContaReceberRecebimento> resultado = recebimentoRepository
                .findAllByTenantIdAndContaReceberIdOrderByRecebidoEmDesc(principal.tenantId(), contaId);

        assertEquals(List.of(idMenor, idMaior), resultado.stream().map(ContaReceberRecebimento::getId).toList());
    }

    @Test
    void pagamentosDevemDesempatarPorIdEPreservarTenant() {
        Fixture principal = criarFixture("PAG-HIST");
        Fixture outroTenant = criarFixture("PAG-OUTRO");
        UUID contaId = inserirContaPagar(principal, "CP-HIST");
        UUID contaOutroTenant = inserirContaPagar(outroTenant, "CP-OUTRO");
        Instant instante = Instant.parse("2026-09-07T13:00:00Z");
        UUID idMenor = UUID.fromString("00000000-0000-0000-0000-000000000201");
        UUID idMaior = UUID.fromString("00000000-0000-0000-0000-000000000202");

        inserirPagamento(principal, contaId, idMaior, instante);
        inserirPagamento(principal, contaId, idMenor, instante);
        inserirPagamento(outroTenant, contaOutroTenant,
                UUID.fromString("00000000-0000-0000-0000-000000000200"), instante.plusSeconds(60));

        List<ContaPagarPagamento> resultado = pagamentoRepository
                .findAllByTenantIdAndContaPagarIdOrderByPagoEmDesc(principal.tenantId(), contaId);

        assertEquals(List.of(idMenor, idMaior), resultado.stream().map(ContaPagarPagamento::getId).toList());
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

    private UUID inserirContaReceber(Fixture f, String documento) {
        UUID id = UUID.randomUUID();
        LocalDate vencimento = LocalDate.of(2026, 10, 10);
        LocalDateTime criadoEm = LocalDateTime.of(2026, 9, 7, 9, 0);
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Historico recebimento', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, f.tenantId(), f.filialId(), f.pessoaId(), documento,
                new BigDecimal("100.00"), BigDecimal.ZERO, vencimento, criadoEm, criadoEm);
        return id;
    }

    private UUID inserirContaPagar(Fixture f, String documento) {
        UUID id = UUID.randomUUID();
        LocalDate vencimento = LocalDate.of(2026, 10, 20);
        LocalDateTime criadoEm = LocalDateTime.of(2026, 9, 7, 10, 0);
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Historico pagamento', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, f.tenantId(), f.filialId(), f.pessoaId(), documento,
                new BigDecimal("100.00"), BigDecimal.ZERO, vencimento, criadoEm, criadoEm);
        return id;
    }

    private void inserirRecebimento(Fixture f, UUID contaId, UUID id, Instant recebidoEm) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber_recebimentos
                    (id, tenant_id, filial_id, conta_receber_id, valor, recebido_em)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, f.tenantId(), f.filialId(), contaId, new BigDecimal("10.00"), recebidoEm);
    }

    private void inserirPagamento(Fixture f, UUID contaId, UUID id, Instant pagoEm) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar_pagamentos
                    (id, tenant_id, filial_id, conta_pagar_id, valor, pago_em)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, f.tenantId(), f.filialId(), contaId, new BigDecimal("10.00"), pagoEm);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
