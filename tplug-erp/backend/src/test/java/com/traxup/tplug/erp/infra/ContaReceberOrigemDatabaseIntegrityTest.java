package com.traxup.tplug.erp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ContaReceberOrigemDatabaseIntegrityTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void deveRejeitarMesmaOrigemNoMesmoTenant() {
        UUID tenantId = inserirTenant("Tenant Origem Unica");
        UUID filialId = inserirFilial(tenantId, "Filial Origem Unica");
        UUID clienteId = inserirPessoa(tenantId, "Cliente Origem Unica");
        UUID pedidoId = UUID.randomUUID();

        inserirContaComOrigem(tenantId, filialId, clienteId, "DOC-1", pedidoId, "PARCELA:1");

        assertThrows(DataIntegrityViolationException.class,
                () -> inserirContaComOrigem(tenantId, filialId, clienteId, "DOC-2", pedidoId, "PARCELA:1"));
    }

    @Test
    void devePermitirMesmaOrigemEmTenantsDiferentes() {
        UUID pedidoId = UUID.randomUUID();
        UUID tenantA = inserirTenant("Tenant Origem A");
        UUID filialA = inserirFilial(tenantA, "Filial Origem A");
        UUID clienteA = inserirPessoa(tenantA, "Cliente Origem A");
        UUID tenantB = inserirTenant("Tenant Origem B");
        UUID filialB = inserirFilial(tenantB, "Filial Origem B");
        UUID clienteB = inserirPessoa(tenantB, "Cliente Origem B");

        inserirContaComOrigem(tenantA, filialA, clienteA, "DOC-A", pedidoId, "PARCELA:1");

        assertDoesNotThrow(() ->
                inserirContaComOrigem(tenantB, filialB, clienteB, "DOC-B", pedidoId, "PARCELA:1"));
    }

    @Test
    void devePermitirContasManuaisSemOrigem() {
        UUID tenantId = inserirTenant("Tenant Manual");
        UUID filialId = inserirFilial(tenantId, "Filial Manual");
        UUID clienteId = inserirPessoa(tenantId, "Cliente Manual");

        assertDoesNotThrow(() -> {
            inserirContaManual(tenantId, filialId, clienteId, "MANUAL-1");
            inserirContaManual(tenantId, filialId, clienteId, "MANUAL-2");
        });
    }

    private UUID inserirTenant(String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", id, nome);
        return id;
    }

    private UUID inserirFilial(UUID tenantId, String nome) {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + nome);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, nome);
        return filialId;
    }

    private UUID inserirPessoa(UUID tenantId, String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor,
                     ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, tenantId, nome);
        return id;
    }

    private void inserirContaComOrigem(UUID tenantId, UUID filialId, UUID clienteId,
                                       String documento, UUID origemId, String referencia) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em,
                     origem_tipo, origem_id, origem_referencia, versao)
                VALUES (?, ?, ?, ?, ?, 'Conta com origem', ?, 0, ?, 'ABERTO',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PEDIDO_VENDA', ?, ?, 0)
                """, UUID.randomUUID(), tenantId, filialId, clienteId, documento,
                new BigDecimal("100.0000"), LocalDate.now().plusDays(30), origemId, referencia);
    }

    private void inserirContaManual(UUID tenantId, UUID filialId, UUID clienteId, String documento) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Conta manual', ?, 0, ?, 'ABERTO',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), tenantId, filialId, clienteId, documento,
                new BigDecimal("100.0000"), LocalDate.now().plusDays(30));
    }
}
