package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TituloFinanceiroResumoApplicationServiceTest {

    @Test
    void resumoReceberDeveReutilizarTenantStatusEPeriodoNormalizados() {
        ContaReceberRepository repository = mock(ContaReceberRepository.class);
        UUID tenantId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);
        when(repository.filtrar(tenantId, "PARCIAL", inicio, fim)).thenReturn(List.of());

        ContaReceberApplicationService service = new ContaReceberApplicationService(
                repository,
                mock(ContaReceberRecebimentoRepository.class),
                mock(FilialRepository.class),
                mock(PessoaRepository.class),
                mock(AuditoriaApplicationService.class));

        TituloFinanceiroResumo resumo = service.resumir(tenantId, " parcial ", inicio, fim);

        assertEquals(0, resumo.quantidade());
        verify(repository).filtrar(tenantId, "PARCIAL", inicio, fim);
    }

    @Test
    void resumoPagarDeveReutilizarTenantStatusEPeriodoNormalizados() {
        ContaPagarRepository repository = mock(ContaPagarRepository.class);
        UUID tenantId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 31);
        when(repository.filtrar(tenantId, "ABERTO", inicio, fim)).thenReturn(List.of());

        ContaPagarApplicationService service = new ContaPagarApplicationService(
                repository,
                mock(ContaPagarPagamentoRepository.class),
                mock(FilialRepository.class),
                mock(PessoaRepository.class),
                mock(AuditoriaApplicationService.class));

        TituloFinanceiroResumo resumo = service.resumir(tenantId, " aberto ", inicio, fim);

        assertEquals(0, resumo.quantidade());
        verify(repository).filtrar(tenantId, "ABERTO", inicio, fim);
    }
}
