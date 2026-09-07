package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TituloFinanceiroFiltroFilialApplicationServiceTest {

    @Test
    void deveFiltrarContasReceberPorTenantEFilial() {
        ContaReceberRepository repository = mock(ContaReceberRepository.class);
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);
        when(repository.filtrarPorFilial(tenantId, filialId, "ABERTO", inicio, fim)).thenReturn(List.of());

        ContaReceberApplicationService service = new ContaReceberApplicationService(
                repository,
                mock(ContaReceberRecebimentoRepository.class),
                mock(FilialRepository.class),
                mock(PessoaRepository.class),
                mock(AuditoriaApplicationService.class));

        assertEquals(0, service.listar(tenantId, filialId, " aberto ", inicio, fim).size());
        verify(repository).filtrarPorFilial(tenantId, filialId, "ABERTO", inicio, fim);
    }

    @Test
    void deveResumirPagarPorTenantEFilial() {
        ContaPagarRepository repository = mock(ContaPagarRepository.class);
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 31);
        when(repository.resumir(tenantId, filialId, null, "PARCIAL", inicio, fim)).thenReturn(resumoVazio());

        ContaPagarApplicationService service = new ContaPagarApplicationService(
                repository,
                mock(ContaPagarPagamentoRepository.class),
                mock(FilialRepository.class),
                mock(PessoaRepository.class),
                mock(AuditoriaApplicationService.class));

        TituloFinanceiroResumo resumo = service.resumir(tenantId, filialId, " parcial ", inicio, fim);

        assertEquals(0, resumo.quantidade());
        verify(repository).resumir(tenantId, filialId, null, "PARCIAL", inicio, fim);
    }

    private static TituloFinanceiroResumoProjection resumoVazio() {
        TituloFinanceiroResumoProjection projection = mock(TituloFinanceiroResumoProjection.class);
        when(projection.getQuantidade()).thenReturn(0L);
        when(projection.getValorOriginalTotal()).thenReturn(BigDecimal.ZERO);
        when(projection.getValorLiquidadoTotal()).thenReturn(BigDecimal.ZERO);
        when(projection.getSaldoAtivoTotal()).thenReturn(BigDecimal.ZERO);
        when(projection.getAbertos()).thenReturn(0L);
        when(projection.getParciais()).thenReturn(0L);
        when(projection.getLiquidados()).thenReturn(0L);
        when(projection.getCancelados()).thenReturn(0L);
        return projection;
    }
}
