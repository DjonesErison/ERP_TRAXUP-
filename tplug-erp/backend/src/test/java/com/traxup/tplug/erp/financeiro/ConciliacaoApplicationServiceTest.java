package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConciliacaoApplicationServiceTest {
    @Mock ConciliacaoLancamentoRepository repository;
    @Mock ContaFinanceiraRepository contaRepository;
    @Mock ContaFinanceiraMovimentoRepository movimentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveImportarLancamentoUsandoContaDoMesmoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.save(any(ConciliacaoLancamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ConciliacaoApplicationService service = novoService();
        service.importar(tenantId, UUID.randomUUID(), contaId, "ofx", "REF-1", "entrada",
                new BigDecimal("50.00"), "Credito", Instant.now());

        verify(contaRepository).findByIdAndTenantId(contaId, tenantId);
        verify(repository).save(any(ConciliacaoLancamento.class));
    }

    @Test
    void naoDeveConciliarMovimentoDeOutraConta() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        UUID movimentoId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, filialId, UUID.randomUUID(), "OFX", "REF-2", "ENTRADA",
                new BigDecimal("75.00"), "Credito", Instant.now(), UUID.randomUUID());
        ContaFinanceiraMovimento movimento = new ContaFinanceiraMovimento(
                tenantId, filialId, UUID.randomUUID(), "ENTRADA", new BigDecimal("75.00"), "Movimento", UUID.randomUUID());
        when(repository.findByIdAndTenantId(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));
        when(movimentoRepository.findByIdAndTenantId(movimentoId, tenantId)).thenReturn(Optional.of(movimento));

        ConciliacaoApplicationService service = novoService();

        assertThrows(IllegalArgumentException.class,
                () -> service.conciliar(tenantId, UUID.randomUUID(), lancamentoId, movimentoId));
        verify(repository).findByIdAndTenantId(lancamentoId, tenantId);
        verify(movimentoRepository).findByIdAndTenantId(movimentoId, tenantId);
    }

    private ConciliacaoApplicationService novoService() {
        ContaFinanceiraApplicationService contaService = new ContaFinanceiraApplicationService(
                contaRepository, movimentoRepository, filialRepository, auditoria);
        return new ConciliacaoApplicationService(repository, contaService, movimentoRepository, auditoria);
    }
}
