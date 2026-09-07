package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.financeiro.ContaReceberApplicationService;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.financeiro.pagamento.FormaPagamentoRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaConsultaRecenteServiceTest {
    @Mock PedidoVendaRepository repository;
    @Mock PedidoVendaItemRepository itemRepository;
    @Mock FilialRepository filialRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    @Mock ContaReceberApplicationService contaReceberService;
    @Mock FormaPagamentoRepository formaPagamentoRepository;
    @Mock CondicaoPagamentoRepository condicaoPagamentoRepository;
    @Mock CondicaoPagamentoParcelaRepository parcelaRepository;
    @Mock AuditoriaApplicationService auditoria;

    private PedidoVendaApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaApplicationService(repository, itemRepository, filialRepository,
                pessoaRepository, estoqueMovimentacaoService, contaReceberService,
                formaPagamentoRepository, condicaoPagamentoRepository, parcelaRepository, auditoria);
    }

    @Test
    void deveConsultarRecentesSomenteDoTenantComLimiteSolicitado() {
        UUID tenantId = UUID.randomUUID();
        PedidoVenda pedido = org.mockito.Mockito.mock(PedidoVenda.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repository.findAllByTenantIdOrderByCriadoEmDescIdAsc(eq(tenantId), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(List.of(pedido));

        var resultado = service.listarRecentes(tenantId, 20);

        assertEquals(List.of(pedido), resultado);
        verify(repository).findAllByTenantIdOrderByCriadoEmDescIdAsc(eq(tenantId), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void deveRejeitarLimiteForaDaFaixaPermitida() {
        assertThrows(IllegalArgumentException.class, () -> service.listarRecentes(UUID.randomUUID(), 0));
        assertThrows(IllegalArgumentException.class, () -> service.listarRecentes(UUID.randomUUID(), 101));
        verifyNoInteractions(repository);
    }

    @Test
    void deveUsarOrdenacaoDeterministicaNaListagemCompleta() {
        UUID tenantId = UUID.randomUUID();
        when(repository.findAllByTenantIdOrderByCriadoEmDescIdAsc(tenantId)).thenReturn(List.of());

        service.listar(tenantId);

        verify(repository).findAllByTenantIdOrderByCriadoEmDescIdAsc(tenantId);
    }
}
