package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.financeiro.ContaFinanceira;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraApplicationService;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraMovimentoRepository;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraRepository;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private IntegracaoFinanceiraApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new IntegracaoFinanceiraApplicationService(repository, contaService, auditoria);
    }
}
