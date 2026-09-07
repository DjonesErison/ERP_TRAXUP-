package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
    void deveDesempatarExtratoDoMesmoInstantePorIdAscendente() {
        var contexto = criarContexto();
        Instant ocorridoEm = Instant.parse("2026-09-07T12:00:00Z");
        UUID primeiroId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID segundoId = UUID.fromString("00000000-0000-0000-0000-000000000012");

        inserirMovimento(segundoId, contexto, ocorridoEm, "ENTRADA", new BigDecimal("100.00"));
        inserirMovimento(primeiroId, contexto, ocorridoEm, "ENTRADA", new BigDecimal("100.00"));

        var resultado = repository.findAllByTenantIdAndContaFinanceiraIdOrderByOcorridoEmDescIdAsc(
                contexto.tenantId(), contexto.contaId());

        assertEquals(2, resultado.size());
        assertEquals(primeiroId, resultado.get(0).getId());
        assertEquals(segundoId, resultado.get(1).getId());
    }

    @Test
    void deveDesempatarCandidatosDoMesmoInstantePorIdAscendente() {
        var contexto = criarContexto();
        Instant ocorridoEm = Instant.parse("2026-09-07T13:00:00Z");
        UUID primeiroId = UUID.fromString("00000000-0000-0000-0000-000000000021");
        UUID segundoId = UUID.fromString("00000000-0000-0000-0000-000000000022");
        BigDecimal valor = new BigDecimal("75.50");

        inserirMovimento(segundoId, contexto, ocorridoEm, "ENTRADA", valor);
        inserirMovimento(primeiroId, contexto, ocorridoEm, "ENTRADA", valor);

        var resultado = repository.findCandidatosDisponiveis(
                contexto.tenantId(), contexto.contaId(), contexto.filialId(), "ENTRADA", valor,
                ocorridoEm.minusSeconds(1), ocorridoEm.plusSeconds(1));

        assertEquals(2, resultado.size());
        assertEquals(primeiroId, resultado.get(0).getId());
        assertEquals(segundoId, resultado.get(1).getId());
    }

    private Contexto criarContexto() {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant Ledger Ordenacao");
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa Ledger Ordenacao");
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial Ledger Ordenacao");
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, 'Conta Ledger Ordenacao', 'BANCO', 0, TRUE,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId);
        return new Contexto(tenantId, filialId, contaId);
    }

    private void inserirMovimento(UUID id, Contexto contexto, Instant ocorridoEm, String tipo, BigDecimal valor) {
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras_movimentos
                    (id, tenant_id, filial_id, conta_financeira_id, tipo, valor, descricao, ocorrido_em)
                VALUES (?, ?, ?, ?, ?, ?, 'Ordenacao deterministica', ?)
                """, id, contexto.tenantId(), contexto.filialId(), contexto.contaId(), tipo, valor, ocorridoEm);
    }

    private record Contexto(UUID tenantId, UUID filialId, UUID contaId) {}
}
