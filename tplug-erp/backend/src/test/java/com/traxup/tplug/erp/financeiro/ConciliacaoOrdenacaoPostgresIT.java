package com.traxup.tplug.erp.financeiro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ConciliacaoOrdenacaoPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConciliacaoLancamentoRepository repository;

    @Test
    void deveDesempatarLancamentosDoMesmoInstantePorIdAscendente() {
        UUID tenantId = inserirTenant();
        UUID filialId = inserirFilial(tenantId);
        UUID contaId = inserirContaFinanceira(tenantId, filialId);
        Instant ocorridoEm = Instant.parse("2026-09-07T12:00:00Z");
        UUID primeiroId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID segundoId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        inserirLancamento(segundoId, tenantId, filialId, contaId, ocorridoEm, "REF-2");
        inserirLancamento(primeiroId, tenantId, filialId, contaId, ocorridoEm, "REF-1");

        var resultado = repository.filtrar(tenantId, contaId, null, null, null, null, null);

        assertEquals(2, resultado.size());
        assertEquals(primeiroId, resultado.get(0).getId());
        assertEquals(segundoId, resultado.get(1).getId());
    }

    private UUID inserirTenant() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, "Tenant Ordenacao Conciliacao");
        return id;
    }

    private UUID inserirFilial(UUID tenantId) {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa Ordenacao Conciliacao");
        jdbcTemplate.update(
                "INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial Ordenacao Conciliacao");
        return filialId;
    }

    private UUID inserirContaFinanceira(UUID tenantId, UUID filialId) {
        UUID contaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO contas_financeiras
                    (id, tenant_id, filial_id, nome, tipo, saldo, ativo, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, 'Conta Ordenacao Conciliacao', 'BANCO', 0, TRUE,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, contaId, tenantId, filialId);
        return contaId;
    }

    private void inserirLancamento(UUID id, UUID tenantId, UUID filialId, UUID contaId,
                                   Instant ocorridoEm, String referencia) {
        jdbcTemplate.update("""
                INSERT INTO conciliacao_lancamentos
                    (id, tenant_id, filial_id, conta_financeira_id, origem, referencia_externa,
                     tipo, valor, descricao, natureza, ocorrido_em, status, criado_em)
                VALUES (?, ?, ?, ?, 'TESTE', ?, 'ENTRADA', 100, 'Ordenacao deterministica',
                        'NORMAL', ?, 'PENDENTE', CURRENT_TIMESTAMP)
                """, id, tenantId, filialId, contaId, referencia, ocorridoEm);
    }
}
