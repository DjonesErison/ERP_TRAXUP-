package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TituloFinanceiroFiltroPessoaApplicationServiceTest {
    @Test
    void deveFiltrarRecebiveisPorTenantClienteEFilial() {
        ContaReceberRepository repository = mock(ContaReceberRepository.class);
        UUID tenant = UUID.randomUUID(), filial = UUID.randomUUID(), cliente = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 9, 1), fim = LocalDate.of(2026, 9, 30);
        when(repository.filtrarPorCliente(tenant, cliente, filial, "ABERTO", inicio, fim)).thenReturn(List.of());
        ContaReceberApplicationService service = new ContaReceberApplicationService(repository, mock(ContaReceberRecebimentoRepository.class), mock(FilialRepository.class), mock(PessoaRepository.class), mock(AuditoriaApplicationService.class));
        assertEquals(0, service.listar(tenant, filial, cliente, " aberto ", inicio, fim).size());
        verify(repository).filtrarPorCliente(tenant, cliente, filial, "ABERTO", inicio, fim);
    }

    @Test
    void deveFiltrarPagaveisPorTenantFornecedorSemFilial() {
        ContaPagarRepository repository = mock(ContaPagarRepository.class);
        UUID tenant = UUID.randomUUID(), fornecedor = UUID.randomUUID();
        when(repository.filtrarPorFornecedor(tenant, fornecedor, null, "PARCIAL", null, null)).thenReturn(List.of());
        ContaPagarApplicationService service = new ContaPagarApplicationService(repository, mock(ContaPagarPagamentoRepository.class), mock(FilialRepository.class), mock(PessoaRepository.class), mock(AuditoriaApplicationService.class));
        assertEquals(0, service.resumir(tenant, null, fornecedor, " parcial ", null, null).quantidade());
        verify(repository).filtrarPorFornecedor(tenant, fornecedor, null, "PARCIAL", null, null);
    }
}
