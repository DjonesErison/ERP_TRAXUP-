package com.traxup.tplug.erp.venda;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void deveAplicarTenantFilialStatusPeriodoELimite() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-09-01T00:00:00Z");
        Instant fim = Instant.parse("2026-09-07T23:59:59Z");
        PedidoVenda pedido = org.mockito.Mockito.mock(PedidoVenda.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repository.buscarRecentesFiltrados(eq(tenantId), eq(filialId), eq("FATURADO"), eq(inicio), eq(fim), any(Pageable.class)))
                .thenReturn(List.of(pedido));

        var service = new PedidoVendaConsultaRecenteService(repository);
        var resultado = service.listar(tenantId, 25, filialId, " faturado ", inicio, fim);

        assertEquals(List.of(pedido), resultado);
        verify(repository).buscarRecentesFiltrados(eq(tenantId), eq(filialId), eq("FATURADO"), eq(inicio), eq(fim), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(25, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void devePermitirFiltrosAusentes() {
        UUID tenantId = UUID.randomUUID();
        when(repository.buscarRecentesFiltrados(eq(tenantId), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(List.of());

        var service = new PedidoVendaConsultaRecenteService(repository);
        service.listar(tenantId, 20, null, " ", null, null);

        verify(repository).buscarRecentesFiltrados(eq(tenantId), eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void deveRejeitarPeriodoInvertidoSemConsultarRepositorio() {
        var service = new PedidoVendaConsultaRecenteService(repository);
        Instant inicio = Instant.parse("2026-09-08T00:00:00Z");
        Instant fim = Instant.parse("2026-09-07T00:00:00Z");

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 20, null, null, inicio, fim));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarStatusDesconhecidoSemConsultarRepositorio() {
        var service = new PedidoVendaConsultaRecenteService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 20, null, "INEXISTENTE", null, null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarLimiteForaDaFaixaSemConsultarRepositorio() {
        var service = new PedidoVendaConsultaRecenteService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.listar(UUID.randomUUID(), 101, null, null, null, null));
        verifyNoInteractions(repository);
    }
}
