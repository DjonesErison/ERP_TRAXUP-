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
class TituloFinanceiroFiltrosIsoladosIntegrationTest {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contasReceberDevemFiltrarSomentePorStatusNoTenant() {
        Base base = criarBase("CR-STATUS");
        UUID outraPessoa = criarPessoa(base.tenantId(), "Pessoa CR status 2");
        Base outroTenant = criarBase("CR-STATUS-OUTRO");

        inserirContaReceber(base.tenantId(), base.filialId(), base.pessoaId(), "ABERTO", LocalDate.of(2027, 1, 10));
        inserirContaReceber(base.tenantId(), base.filialId(), outraPessoa, "PARCIAL", LocalDate.of(2027, 1, 11));
        inserirContaReceber(outroTenant.tenantId(), outroTenant.filialId(), outroTenant.pessoaId(), "PARCIAL", LocalDate.of(2027, 1, 12));

        List<ContaReceber> resultado = contaReceberRepository.filtrar(base.tenantId(), "PARCIAL", null, null);

        assertEquals(1, resultado.size());
        assertEquals(outraPessoa, resultado.getFirst().getClienteId());
        assertEquals("PARCIAL", resultado.getFirst().getStatus());
        assertEquals(base.tenantId(), resultado.getFirst().getTenantId());
    }

    @Test
    void contasPagarDevemFiltrarSomentePorStatusNoTenant() {
        Base base = criarBase("CP-STATUS");
        UUID outraPessoa = criarPessoa(base.tenantId(), "Pessoa CP status 2");
        Base outroTenant = criarBase("CP-STATUS-OUTRO");

        inserirContaPagar(base.tenantId(), base.filialId(), base.pessoaId(), "ABERTO", LocalDate.of(2027, 2, 10));
        inserirContaPagar(base.tenantId(), base.filialId(), outraPessoa, "PARCIAL", LocalDate.of(2027, 2, 11));
        inserirContaPagar(outroTenant.tenantId(), outroTenant.filialId(), outroTenant.pessoaId(), "PARCIAL", LocalDate.of(2027, 2, 12));

        List<ContaPagar> resultado = contaPagarRepository.filtrar(base.tenantId(), "PARCIAL", null, null);

        assertEquals(1, resultado.size());
        assertEquals(outraPessoa, resultado.getFirst().getFornecedorId());
        assertEquals("PARCIAL", resultado.getFirst().getStatus());
        assertEquals(base.tenantId(), resultado.getFirst().getTenantId());
    }

    @Test
    void contasReceberDevemFiltrarSomentePorFilial() {
        Base base = criarBase("CR-FILIAL");
        UUID outraFilial = criarFilial(base.tenantId(), base.empresaId(), "Filial CR alternativa");

        inserirContaReceber(base.tenantId(), base.filialId(), base.pessoaId(), "ABERTO", LocalDate.of(2027, 3, 10));
        inserirContaReceber(base.tenantId(), outraFilial, base.pessoaId(), "ABERTO", LocalDate.of(2027, 3, 11));

        List<ContaReceber> resultado = contaReceberRepository.filtrarPorFilial(
                base.tenantId(), base.filialId(), null, null, null);

        assertEquals(1, resultado.size());
        assertEquals(base.filialId(), resultado.getFirst().getFilialId());
        assertTrue(resultado.stream().allMatch(c -> base.tenantId().equals(c.getTenantId())));
    }

    @Test
    void contasPagarDevemFiltrarSomentePorFilial() {
        Base base = criarBase("CP-FILIAL");
        UUID outraFilial = criarFilial(base.tenantId(), base.empresaId(), "Filial CP alternativa");

        inserirContaPagar(base.tenantId(), base.filialId(), base.pessoaId(), "ABERTO", LocalDate.of(2027, 4, 10));
        inserirContaPagar(base.tenantId(), outraFilial, base.pessoaId(), "ABERTO", LocalDate.of(2027, 4, 11));

        List<ContaPagar> resultado = contaPagarRepository.filtrarPorFilial(
                base.tenantId(), base.filialId(), null, null, null);

        assertEquals(1, resultado.size());
        assertEquals(base.filialId(), resultado.getFirst().getFilialId());
        assertTrue(resultado.stream().allMatch(c -> base.tenantId().equals(c.getTenantId())));
    }

    @Test
    void contasReceberDevemFiltrarSomentePorClienteSemExigirFilial() {
        Base base = criarBase("CR-CLIENTE");
        UUID outroCliente = criarPessoa(base.tenantId(), "Cliente CR alternativo");
        UUID outraFilial = criarFilial(base.tenantId(), base.empresaId(), "Filial CR cliente alternativa");

        inserirContaReceber(base.tenantId(), base.filialId(), base.pessoaId(), "ABERTO", LocalDate.of(2027, 5, 10));
        inserirContaReceber(base.tenantId(), outraFilial, base.pessoaId(), "ABERTO", LocalDate.of(2027, 5, 11));
        inserirContaReceber(base.tenantId(), base.filialId(), outroCliente, "ABERTO", LocalDate.of(2027, 5, 12));

        List<ContaReceber> resultado = contaReceberRepository.filtrarPorCliente(
                base.tenantId(), base.pessoaId(), null, null, null, null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> base.pessoaId().equals(c.getClienteId())));
        assertTrue(resultado.stream().allMatch(c -> base.tenantId().equals(c.getTenantId())));
    }

    @Test
    void contasPagarDevemFiltrarSomentePorFornecedorSemExigirFilial() {
        Base base = criarBase("CP-FORNECEDOR");
        UUID outroFornecedor = criarPessoa(base.tenantId(), "Fornecedor CP alternativo");
        UUID outraFilial = criarFilial(base.tenantId(), base.empresaId(), "Filial CP fornecedor alternativa");

        inserirContaPagar(base.tenantId(), base.filialId(), base.pessoaId(), "ABERTO", LocalDate.of(2027, 6, 10));
        inserirContaPagar(base.tenantId(), outraFilial, base.pessoaId(), "ABERTO", LocalDate.of(2027, 6, 11));
        inserirContaPagar(base.tenantId(), base.filialId(), outroFornecedor, "ABERTO", LocalDate.of(2027, 6, 12));

        List<ContaPagar> resultado = contaPagarRepository.filtrarPorFornecedor(
                base.tenantId(), base.pessoaId(), null, null, null, null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> base.pessoaId().equals(c.getFornecedorId())));
        assertTrue(resultado.stream().allMatch(c -> base.tenantId().equals(c.getTenantId())));
    }

    private Base criarBase(String sufixo) {
        UUID tenantId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO tenants (id, nome) VALUES (?, ?)", tenantId, "Tenant " + sufixo);
        jdbcTemplate.update("INSERT INTO empresas (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaId, tenantId, "Empresa " + sufixo);
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                filialId, tenantId, empresaId, "Filial " + sufixo);
        inserirPessoa(tenantId, pessoaId, "Pessoa " + sufixo);

        return new Base(tenantId, empresaId, filialId, pessoaId);
    }

    private UUID criarFilial(UUID tenantId, UUID empresaId, String nome) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO filiais (id, tenant_id, empresa_id, nome) VALUES (?, ?, ?, ?)",
                id, tenantId, empresaId, nome);
        return id;
    }

    private UUID criarPessoa(UUID tenantId, String nome) {
        UUID id = UUID.randomUUID();
        inserirPessoa(tenantId, id, nome);
        return id;
    }

    private void inserirPessoa(UUID tenantId, UUID pessoaId, String nome) {
        jdbcTemplate.update("""
                INSERT INTO pessoas
                    (id, tenant_id, tipo_pessoa, nome_razao_social, cliente, fornecedor, ativo, criado_em, atualizado_em)
                VALUES (?, ?, 'JURIDICA', ?, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, pessoaId, tenantId, nome);
    }

    private void inserirContaReceber(UUID tenantId, UUID filialId, UUID clienteId,
                                      String status, LocalDate vencimento) {
        jdbcTemplate.update("""
                INSERT INTO contas_receber
                    (id, tenant_id, filial_id, cliente_id, numero_documento, descricao,
                     valor_original, valor_recebido, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste filtro isolado', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), tenantId, filialId, clienteId, "CR-" + UUID.randomUUID(),
                new BigDecimal("100.00"), BigDecimal.ZERO, vencimento, status);
    }

    private void inserirContaPagar(UUID tenantId, UUID filialId, UUID fornecedorId,
                                    String status, LocalDate vencimento) {
        jdbcTemplate.update("""
                INSERT INTO contas_pagar
                    (id, tenant_id, filial_id, fornecedor_id, numero_documento, descricao,
                     valor_original, valor_pago, vencimento, status, criado_em, atualizado_em, versao)
                VALUES (?, ?, ?, ?, ?, 'Teste filtro isolado', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, UUID.randomUUID(), tenantId, filialId, fornecedorId, "CP-" + UUID.randomUUID(),
                new BigDecimal("100.00"), BigDecimal.ZERO, vencimento, status);
    }

    private record Base(UUID tenantId, UUID empresaId, UUID filialId, UUID pessoaId) {}
}
