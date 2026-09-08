package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.financeiro.ContaReceberApplicationService;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.financeiro.pagamento.FormaPagamentoRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaBuscaPorNumeroServiceTest {
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
    void deveNormalizarNumeroEBuscarSomenteNoTenantInformado() {
        UUID tenantId = UUID.randomUUID();
        PedidoVenda pedido = org.mockito.Mockito.mock(PedidoVenda.class);
        when(repository.findByTenantIdAndNumeroIgnoreCase(tenantId, "PV-123")).thenReturn(Optional.of(pedido));

        assertEquals(pedido, service.buscarPorNumero(tenantId, "  PV-123  "));

        verify(repository).findByTenantIdAndNumeroIgnoreCase(tenantId, "PV-123");
    }

    @Test
    void deveRetornarNaoEncontradoSemConsultarOutroTenant() {
        UUID tenantId = UUID.randomUUID();
        when(repository.findByTenantIdAndNumeroIgnoreCase(tenantId, "PV-404")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.buscarPorNumero(tenantId, "PV-404"));

        verify(repository).findByTenantIdAndNumeroIgnoreCase(tenantId, "PV-404");
    }

    @Test
    void deveRejeitarNumeroEmBrancoSemConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buscarPorNumero(UUID.randomUUID(), "   "));
        verifyNoInteractions(repository);
    }
}
