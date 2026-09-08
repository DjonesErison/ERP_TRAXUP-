package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaConsultaRecenteFiltroServiceTest {
    @Mock PedidoVendaRepository repository;

    @Test
    void deveAplicarTenantFilialClienteNumeroFormaPagamentoStatusPeriodoEPaginacao() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID formaPagamentoId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-09-01T00:00:00Z");
        Instant fim = Instant.parse("2026-09-07T23:59:59Z");
        PedidoVenda pedido = org.mockito.Mockito.mock(PedidoVenda.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repository.buscarRecentesFiltradosPaginado(eq(tenantId), eq(filialId), eq(clienteId), eq("PV-123"),
                eq(formaPagamentoId), eq("FATURADO"), eq(inicio), eq(fim), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(pedido), inv.getArgument(8), 1));

        var service = new PedidoVendaConsultaRecenteService(repository);
        var resultado = service.listar(tenantId, 2, 25, filialId, clienteId, "PV-123", formaPagamentoId,
                " faturado ", inicio, fim);

        assertEquals(List.of(pedido), resultado.getContent());
        verify(repository).buscarRecentesFiltradosPaginado(eq(tenantId), eq(filialId), eq(clienteId), eq("PV-123"),
                eq(formaPagamentoId), eq("FATURADO"), eq(inicio), eq(fim), pageableCaptor.capture());
        assertEquals(2, pageableCaptor.getValue().getPageNumber());
        assertEquals(25, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void devePermitirFiltrosAusentes() {
        UUID tenantId = UUID.randomUUID();
        when(repository.buscarRecentesFiltradosPaginado(eq(tenantId), eq(null), eq(null), eq(null), eq(null), eq(null),
                eq(null), eq(null), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(8), 0));

        var service = new PedidoVendaConsultaRecenteService(repository);
        service.listar(tenantId, 0, 20, null, null, " ", null, " ", null, null);

        verify(repository).buscarRecentesFiltradosPaginado(eq(tenantId), eq(null), eq(null), eq(null), eq(null), eq(null),
                eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void deveRejeitarPeriodoInvertidoSemConsultarRepositorio() {
        var service = new PedidoVendaConsultaRecenteService(repository);
        Instant inicio = Instant.parse("2026-09-08T00:00:00Z");
        Instant fim = Instant.parse("2026-09-07T00:00:00Z");

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 0, 20, null, null, null, null, null, inicio, fim));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarStatusDesconhecidoSemConsultarRepositorio() {
        var service = new PedidoVendaConsultaRecenteService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 0, 20, null, null, null, null, "INEXISTENTE", null, null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarTamanhoForaDaFaixaSemConsultarRepositorio() {
        var service = new PedidoVendaConsultaRecenteService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 0, 101, null, null, null, null, null, null, null));
        verifyNoInteractions(repository);
    }
}
