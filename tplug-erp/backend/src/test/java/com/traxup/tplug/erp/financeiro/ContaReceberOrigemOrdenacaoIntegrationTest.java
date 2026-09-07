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
class ContaReceberOrigemOrdenacaoIntegrationTest {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveIsolarTenantEOrigemEOrdenarDeterministicamente() {
        Fixture principal = criarFixture("ORIGEM-A");
        Fixture outroTenant = criarFixture("ORIGEM-B");
        UUID origemId = UUID.randomUUID();
        UUID outraOrigemId = UUID.randomUUID();
        LocalDate vencimento = LocalDate.of(2026, 11, 10);
        LocalDateTime antiga = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime recente = LocalDateTime.of(2026, 9, 2, 9, 0);

        UUID idAntigo = UUID.fromString("00000000-0000-0000-0000-000000000103");
        UUID idEmpateMenor = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID idEmpateMaior = UUID.fromString("00000000-0000-0000-0000-000000000102");

        inserirContaReceber(principal, idAntigo, origemId, vencimento, antiga, "A-ANTIGO");
        inserirContaReceber(principal, idEmpateMaior, origemId, vencimento, recente, "A-MAIOR");
        inserirContaReceber(principal, idEmpateMenor, origemId, vencimento, recente, "A-MENOR");
        inserirContaReceber(principal, UUID.randomUUID(), outraOrigemId, vencimento, recente, "OUTRA-ORIGEM");
        inserirContaReceber(outroTenant, UUID.randomUUID(), origemId, vencimento, recente, "OUTRO-TENANT");

        List<ContaReceber> resultado = contaReceberRepository
                .findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDescIdAsc(
                        principal.tenantId(), "PEDIDO_VENDA", origemId);

        assertEquals(List.of(idEmpateMenor, idEmpateMaior, idAntigo),
                resultado.stream().map(ContaReceber::getId).toList());
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
                VALUES (?, ?, 'JURIDICA', ?, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, "Cliente " + sufixo);

        return new Fixture(tenantId, filialId, pessoaId);
    }

    private void inserirContaReceber(Fixture f, UUID id, UUID origemId, LocalDate vencimento,
                                      LocalDateTime criadoEm, String referencia) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao,
                     origem_tipo, origem_id, origem_referencia)
                VALUES (?, ?, ?, ?, ?, 'Teste origem', ?, ?, ?, 'ABERTO', ?, ?, 0, 'PEDIDO_VENDA', ?, ?)
                """, id, f.tenantId(), f.filialId(), f.pessoaId(), "CR-" + id,
                new BigDecimal("10.00"), BigDecimal.ZERO, vencimento, criadoEm, criadoEm, origemId, referencia);
    }

    private record Fixture(UUID tenantId, UUID filialId, UUID pessoaId) {}
}
