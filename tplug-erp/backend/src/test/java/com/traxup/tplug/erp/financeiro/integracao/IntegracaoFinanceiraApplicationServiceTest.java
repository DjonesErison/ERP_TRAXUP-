package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.financeiro.ContaFinanceira;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraApplicationService;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraMovimentoRepository;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraRepository;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoFinanceiraApplicationServiceTest {
    @Mock IntegracaoFinanceiraRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveCriarIntegracaoSomenteNaContaDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, filialId, "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.save(any(IntegracaoFinanceira.class))).thenAnswer(inv -> inv.getArgument(0));

        IntegracaoFinanceira integracao = novoService().criar(tenantId, UUID.randomUUID(), contaId,
                "psp_teste", "merchant-123");

        assertEquals("PSP_TESTE", integracao.getProvedor());
        assertEquals(filialId, integracao.getFilialId());
        verify(contaRepository).findByIdAndTenantId(contaId, tenantId);
        verify(repository).existsByTenantIdAndContaFinanceiraIdAndProvedor(tenantId, contaId, "PSP_TESTE");
    }

    @Test
    void deveBloquearProvedorDuplicadoNaMesmaConta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.existsByTenantIdAndContaFinanceiraIdAndProvedor(tenantId, contaId, "PSP_TESTE"))
                .thenReturn(true);

        assertThrows(RecursoConflitanteException.class,
                () -> novoService().criar(tenantId, UUID.randomUUID(), contaId, "psp_teste", null));
    }

    @Test
    void deveRegistrarCheckpointSomenteNaIntegracaoDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID integracaoId = UUID.randomUUID();
        IntegracaoFinanceira integracao = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "PSP_TESTE", null, UUID.randomUUID());
        Instant sincronizadoEm = Instant.parse("2026-09-06T08:00:00Z");
        when(repository.findByIdAndTenantId(integracaoId, tenantId)).thenReturn(Optional.of(integracao));
        when(repository.save(any(IntegracaoFinanceira.class))).thenAnswer(inv -> inv.getArgument(0));

        IntegracaoFinanceira resultado = novoService().registrarSincronizacao(
                tenantId, UUID.randomUUID(), integracaoId, " cursor-42 ", sincronizadoEm);

        assertEquals("cursor-42", resultado.getCheckpoint());
        assertEquals(sincronizadoEm, resultado.getSincronizadoEm());
        verify(repository).findByIdAndTenantId(integracaoId, tenantId);
    }

    @Test
    void naoDeveAcessarCheckpointDeOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID integracaoId = UUID.randomUUID();
        when(repository.findByIdAndTenantId(integracaoId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> novoService().registrarSincronizacao(
                        tenantId, UUID.randomUUID(), integracaoId, "cursor", Instant.now()));
    }

    @Test
    void naoDeveSincronizarIntegracaoInativa() {
        UUID tenantId = UUID.randomUUID();
        UUID integracaoId = UUID.randomUUID();
        IntegracaoFinanceira integracao = new IntegracaoFinanceira(
                tenantId, UUID.randomUUID(), UUID.randomUUID(), "PSP_TESTE", null, UUID.randomUUID());
        integracao.desativar();
        when(repository.findByIdAndTenantId(integracaoId, tenantId)).thenReturn(Optional.of(integracao));

        assertThrows(RecursoConflitanteException.class,
                () -> novoService().registrarSincronizacao(
                        tenantId, UUID.randomUUID(), integracaoId, "cursor", Instant.now()));
    }

    private IntegracaoFinanceiraApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new IntegracaoFinanceiraApplicationService(repository, contaService, auditoria);
    }
}
