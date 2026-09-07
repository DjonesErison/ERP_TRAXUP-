package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TituloFinanceiroResumoAgregadoApplicationServiceTest {

    @Test
    void deveResumirContasReceberDiretoNoRepositorioAgregado() {
        ContaReceberRepository repository = mock(ContaReceberRepository.class);
        TituloFinanceiroResumoProjection projection = mock(TituloFinanceiroResumoProjection.class);
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);
        when(projection.getQuantidade()).thenReturn(3L);
        when(projection.getValorOriginalTotal()).thenReturn(new BigDecimal("300.00"));
        when(projection.getValorLiquidadoTotal()).thenReturn(new BigDecimal("80.00"));
        when(projection.getSaldoAtivoTotal()).thenReturn(new BigDecimal("220.00"));
        when(projection.getAbertos()).thenReturn(2L);
        when(projection.getParciais()).thenReturn(1L);
        when(projection.getLiquidados()).thenReturn(0L);
        when(projection.getCancelados()).thenReturn(0L);
        when(repository.resumir(tenantId, filialId, clienteId, "ABERTO", inicio, fim)).thenReturn(projection);

        ContaReceberApplicationService service = new ContaReceberApplicationService(
                repository, mock(ContaReceberRecebimentoRepository.class), mock(FilialRepository.class),
                mock(PessoaRepository.class), mock(AuditoriaApplicationService.class));

        TituloFinanceiroResumo resumo = service.resumir(tenantId, filialId, clienteId, " aberto ", inicio, fim);

        assertEquals(3, resumo.quantidade());
        assertEquals(new BigDecimal("220.00"), resumo.saldoAtivoTotal());
        verify(repository).resumir(tenantId, filialId, clienteId, "ABERTO", inicio, fim);
        verify(repository, never()).filtrarPorCliente(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveResumirContasPagarDiretoNoRepositorioAgregado() {
        ContaPagarRepository repository = mock(ContaPagarRepository.class);
        TituloFinanceiroResumoProjection projection = mock(TituloFinanceiroResumoProjection.class);
        UUID tenantId = UUID.randomUUID();
        UUID fornecedorId = UUID.randomUUID();
        when(projection.getQuantidade()).thenReturn(1L);
        when(projection.getValorOriginalTotal()).thenReturn(new BigDecimal("50.00"));
        when(projection.getValorLiquidadoTotal()).thenReturn(BigDecimal.ZERO);
        when(projection.getSaldoAtivoTotal()).thenReturn(new BigDecimal("50.00"));
        when(projection.getAbertos()).thenReturn(1L);
        when(projection.getParciais()).thenReturn(0L);
        when(projection.getLiquidados()).thenReturn(0L);
        when(projection.getCancelados()).thenReturn(0L);
        when(repository.resumir(tenantId, null, fornecedorId, null, null, null)).thenReturn(projection);

        ContaPagarApplicationService service = new ContaPagarApplicationService(
                repository, mock(ContaPagarPagamentoRepository.class), mock(FilialRepository.class),
                mock(PessoaRepository.class), mock(AuditoriaApplicationService.class));

        TituloFinanceiroResumo resumo = service.resumir(tenantId, null, fornecedorId, null, null, null);

        assertEquals(1, resumo.quantidade());
        assertEquals(new BigDecimal("50.00"), resumo.saldoAtivoTotal());
        verify(repository).resumir(tenantId, null, fornecedorId, null, null, null);
        verify(repository, never()).filtrarPorFornecedor(any(), any(), any(), any(), any(), any());
    }
}
