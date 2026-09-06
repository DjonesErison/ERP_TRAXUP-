package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
    void deveImportarLoteNaMesmaContaDoTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        ContaFinanceira conta = new ContaFinanceira(tenantId, UUID.randomUUID(), "Banco", "BANCO", UUID.randomUUID());
        when(contaRepository.findByIdAndTenantId(contaId, tenantId)).thenReturn(Optional.of(conta));
        when(repository.save(any(ConciliacaoLancamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        var itens = List.of(
                new ConciliacaoApplicationService.ImportacaoLancamento("api", "L1", "entrada", new BigDecimal("10.00"), "Credito 1", Instant.now()),
                new ConciliacaoApplicationService.ImportacaoLancamento("api", "L2", "saida", new BigDecimal("5.00"), "Debito 1", Instant.now())
        );

        List<ConciliacaoLancamento> resultado = novoService().importarLote(tenantId, UUID.randomUUID(), contaId, itens);

        assertEquals(2, resultado.size());
        verify(contaRepository, times(2)).findByIdAndTenantId(contaId, tenantId);
        verify(repository, times(2)).save(any(ConciliacaoLancamento.class));
    }

    @Test
    void deveSugerirMovimentosCompativeisEmJanelaDeTresDias() {
        UUID tenantId = UUID.randomUUID();
        UUID lancamentoId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Instant ocorridoEm = Instant.parse("2026-09-05T12:00:00Z");
        BigDecimal valor = new BigDecimal("125.50");
        ConciliacaoLancamento lancamento = new ConciliacaoLancamento(
                tenantId, filialId, contaId, "OFX", "REF-SUG", "ENTRADA", valor,
                "Credito", ocorridoEm, UUID.randomUUID());
        ContaFinanceiraMovimento movimento = new ContaFinanceiraMovimento(
                tenantId, filialId, contaId, "ENTRADA", valor, "Recebimento", UUID.randomUUID());
        when(repository.findByIdAndTenantId(lancamentoId, tenantId)).thenReturn(Optional.of(lancamento));
        when(movimentoRepository.findAllByTenantIdAndContaFinanceiraIdAndFilialIdAndTipoAndValorAndOcorridoEmBetweenOrderByOcorridoEmAsc(
                tenantId, contaId, filialId, "ENTRADA", valor,
                ocorridoEm.minusSeconds(259200), ocorridoEm.plusSeconds(259200)))
                .thenReturn(List.of(movimento));

        List<ContaFinanceiraMovimento> sugestoes = novoService().sugerirMovimentos(tenantId, lancamentoId);

        assertEquals(1, sugestoes.size());
        verify(movimentoRepository).findAllByTenantIdAndContaFinanceiraIdAndFilialIdAndTipoAndValorAndOcorridoEmBetweenOrderByOcorridoEmAsc(
                tenantId, contaId, filialId, "ENTRADA", valor,
                ocorridoEm.minusSeconds(259200), ocorridoEm.plusSeconds(259200));
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
