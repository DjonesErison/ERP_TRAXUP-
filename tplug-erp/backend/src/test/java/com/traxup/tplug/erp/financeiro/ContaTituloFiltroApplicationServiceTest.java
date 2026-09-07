package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaTituloFiltroApplicationServiceTest {
    @Mock ContaReceberRepository receberRepository;
    @Mock ContaReceberRecebimentoRepository recebimentoRepository;
    @Mock ContaPagarRepository pagarRepository;
    @Mock ContaPagarPagamentoRepository pagamentoRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock AuditoriaApplicationService auditoria;

    @Test
    void deveNormalizarStatusEManterTenantNoFiltroDeContasAReceber() {
        UUID tenantId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);
        when(receberRepository.filtrar(tenantId, "PARCIAL", inicio, fim)).thenReturn(List.of());

        receberService().listar(tenantId, " parcial ", inicio, fim);

        verify(receberRepository).filtrar(tenantId, "PARCIAL", inicio, fim);
    }

    @Test
    void deveRejeitarStatusInvalidoEmContasAReceberAntesDoRepositorio() {
        assertThrows(RegraNegocioException.class,
                () -> receberService().listar(UUID.randomUUID(), "PAGO", null, null));
        verifyNoInteractions(receberRepository);
    }

    @Test
    void deveNormalizarStatusEManterTenantNoFiltroDeContasAPagar() {
        UUID tenantId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 31);
        when(pagarRepository.filtrar(tenantId, "PAGO", inicio, fim)).thenReturn(List.of());

        pagarService().listar(tenantId, " pago ", inicio, fim);

        verify(pagarRepository).filtrar(tenantId, "PAGO", inicio, fim);
    }

    @Test
    void deveRejeitarPeriodoInvertidoEmContasAPagarAntesDoRepositorio() {
        LocalDate inicio = LocalDate.of(2026, 11, 2);
        LocalDate fim = LocalDate.of(2026, 11, 1);

        assertThrows(RegraNegocioException.class,
                () -> pagarService().listar(UUID.randomUUID(), null, inicio, fim));
        verifyNoInteractions(pagarRepository);
    }

    private ContaReceberApplicationService receberService() {
        return new ContaReceberApplicationService(receberRepository, recebimentoRepository,
                filialRepository, pessoaRepository, auditoria);
    }

    private ContaPagarApplicationService pagarService() {
        return new ContaPagarApplicationService(pagarRepository, pagamentoRepository,
                filialRepository, pessoaRepository, auditoria);
    }
}
