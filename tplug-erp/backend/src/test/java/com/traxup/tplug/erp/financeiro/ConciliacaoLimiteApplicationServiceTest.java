package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConciliacaoLimiteApplicationServiceTest {
    @Mock ConciliacaoLancamentoRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveAplicarLimiteInformadoPreservandoTenantEConta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.filtrar(tenantId, contaId, "OFX", null, "PENDENTE", null, null, null,
                PageRequest.of(0, 25))).thenReturn(List.of());

        novoService().listar(tenantId, contaId, " ofx ", null, "pendente", null, null, null, 25);

        verify(repository).filtrar(tenantId, contaId, "OFX", null, "PENDENTE", null, null, null,
                PageRequest.of(0, 25));
    }

    @Test
    void deveRejeitarLimiteZeroAntesDaConsulta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));

        assertThrows(RegraNegocioException.class, () -> novoService().listar(
                tenantId, contaId, null, null, null, null, null, null, 0));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarLimiteAcimaDeQuinhentosAntesDaConsulta() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));

        assertThrows(RegraNegocioException.class, () -> novoService().listar(
                tenantId, contaId, null, null, null, null, null, null, 501));

        verifyNoInteractions(repository);
    }

    private ConciliacaoApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new ConciliacaoApplicationService(repository, contaService, movimentoRepository, auditoria);
    }
}
