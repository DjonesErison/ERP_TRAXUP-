package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class TituloFinanceiroOrdenacaoIntegrationTest {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contaReceberDeveOrdenarPorVencimentoCriacaoDescEId() {
        Fixture f = criarFixture("CR-ORDEM");
        LocalDate vencimento = LocalDate.of(2026, 10, 10);
        LocalDateTime antiga = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime recente = LocalDateTime.of(2026, 9, 2, 9, 0);

        UUID idAntigo = UUID.fromString("00000000-0000-0000-0000-000000000003");
        UUID idEmpateMenor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID idEmpateMaior = UUID.fromString("00000000-0000-0000-0000-000000000002");

        inserirContaReceber(f, idAntigo, vencimento, antiga);
        inserirContaReceber(f, idEmpateMaior, vencimento, recente);
        inserirContaReceber(f, idEmpateMenor, vencimento, recente);

        List<ContaReceber> resultado = contaReceberRepository.filtrarPorCliente(
                f.tenantId(), f.pessoaId(), f.filialId(), "ABERTO", vencimento, vencimento);

        assertEquals(List.of(idEmpateMenor, idEmpateMaior, idAntigo),
                resultado.stream().map(ContaReceber::getId).toList());
    }

    @Test
    void contaPagarDeveOrdenarPorVencimentoCriacaoDescEId() {
        Fixture f = criarFixture("CP-ORDEM");
        LocalDate vencimento = LocalDate.of(2026, 10, 20);
        LocalDateTime antiga = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime recente = LocalDateTime.of(2026, 9, 2, 10, 0);

        UUID idAntigo = UUID.fromString("00000000-0000-0000-0000-000000000013");
        UUID idEmpateMenor = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID idEmpateMaior = UUID.fromString("00000000-0000-0000-0000-000000000012");

        inserirContaPagar(f, idAntigo, vencimento, antiga);
        inserirContaPagar(f, idEmpateMaior, vencimento, recente);
        inserirContaPagar(f, idEmpateMenor, vencimento, recente);

        List<ContaPagar> resultado = contaPagarRepository.filtrarPorFornecedor(
                f.tenantId(), f.pessoaId(), f.filialId(), "ABERTO", vencimento, vencimento);

        assertEquals(List.of(idEmpateMenor, idEmpateMaior, idAntigo),
                resultado.stream().map(ContaPagar::getId).toList());
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

    private void inserirContaReceber(Fixture f, UUID id, LocalDate vencimento, LocalDateTime criadoEm) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste ordenacao', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, f.tenantId(), f.filialId(), f.pessoaId(), "CR-" + id,
                new BigDecimal("10.00"), BigDecimal.ZERO, vencimento, criadoEm, criadoEm);
    }

    private void inserirContaPagar(Fixture f, UUID id, LocalDate vencimento, LocalDateTime criadoEm) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste ordenacao', ?, ?, ?, 'ABERTO', ?, ?, 0)
                """, id, f.tenantId(), f.filialId(), f.pessoaId(), "CP-" + id,
                new BigDecimal("10.00"), BigDecimal.ZERO, vencimento, criadoEm, criadoEm);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
