package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ContaFinanceiraMovimentoOrdenacaoPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ContaFinanceiraMovimentoRepository repository;

    @Test
    void deveDesempatarListagemDoMesmoInstantePorIdAscendente() {
        UUID tenantId = inserirTenant();
        UUID filialId = inserirFilial(tenantId);
        UUID contaId = inserirContaFinanceira(tenantId, filialId);
        Instant ocorridoEm = Instant.parse("2026-09-07T13:00:00Z");
        UUID primeiroId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID segundoId = UUID.fromString("00000000-0000-0000-0000-000000000012");

        inserirMovimento(segundoId, tenantId, filialId, contaId, ocorridoEm);
        inserirMovimento(primeiroId, tenantId, filialId, contaId, ocorridoEm);

        var resultado = repository.findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDescIdAsc(tenantId, contaId);

        assertEquals(2, resultado.size());
        assertEquals(primeiroId, resultado.get(0).getId());
        assertEquals(segundoId, resultado.get(1).getId());
    }

    @Test
    void deveDesempatarCandidatosDoMesmoInstantePorIdAscendente() {
        UUID tenantId = inserirTenant();
        UUID filialId = inserirFilial(tenantId);
        UUID contaId = inserirContaFinanceira(tenantId, filialId);
        Instant ocorridoEm = Instant.parse("2026-09-07T14:00:00Z");
        UUID primeiroId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        UUID segundoId = UUID.fromString("00000000-0000-0000-0000-000000000022");

        inserirMovimento(segundoId, tenantId, filialId, contaId, ocorridoEm);
        inserirMovimento(primeiroId, tenantId, filialId, contaId, ocorridoEm);

        var resultado = repository.findCandidatosDisponiveis(
                tenantId, contaId, filialId, "ENTRADA", new BigDecimal("100.0000"),
                ocorridoEm.minus(1, ChronoUnit.MINUTES), ocorridoEm.plus(1, ChronoUnit.MINUTES));

        assertEquals(2, resultado.size());
        assertEquals(primeiroId, resultado.get(0).getId());
        assertEquals(segundoId, resultado.get(1).getId());
    }

    private UUID inserirTenant() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, "Tenant Ordenacao Movimentos");
        return id;
    }

    private UUID inserirFilial(UUID tenantId) {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa Ordenacao Movimentos");
        jdbcTemplate.update(
                "INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial Ordenacao Movimentos");
        return filialId;
    }

    private UUID inserirContaFinanceira(UUID tenantId, UUID filialId) {
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, 'Conta Ordenacao Movimentos', 'BANCO', 0, TRUE,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId);
        return contaId;
    }

    private void inserirMovimento(UUID id, UUID tenantId, UUID filialId, UUID contaId, Instant ocorridoEm) {
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras_movimentos
                    (id, tenant_id, filial_id, conta_financeira_id, tipo, valor, descricao, ocorrido_em)
                VALUES (?, ?, ?, ?, 'ENTRADA', 100, 'Ordenacao deterministica', ?)
                """, id, tenantId, filialId, contaId, ocorridoEm);
    }
}
