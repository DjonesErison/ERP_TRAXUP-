package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaConsultaRecenteService;
import com.traxup.tplug.erp.venda.PedidoVendaDetalheConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaControllerConsultaTest {
    @Mock PedidoVendaApplicationService service;
    @Mock PedidoVendaConsultaRecenteService consultaRecenteService;
    @Mock PedidoVendaDetalheConsultaService detalheConsultaService;
    @Mock TenantContext tenantContext;

    private PedidoVendaController controller;

    @BeforeEach
    void setUp() {
        controller = new PedidoVendaController(service, consultaRecenteService, detalheConsultaService, tenantContext);
    }

    @Test
    void deveUsarTenantDoContextoNaConsultaDeTotaisSemLookupDuplicado() {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(detalheConsultaService.consultar(tenantId, pedidoId))
                .thenReturn(new PedidoVendaDetalheConsultaService.Detalhe(mock(PedidoVenda.class), List.of()));

        controller.totais(pedidoId);

        verify(detalheConsultaService).consultar(tenantId, pedidoId);
        verifyNoInteractions(service, consultaRecenteService);
    }

    @Test
    void deveUsarTenantDoContextoNaConsultaDeVendasRecentes() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(consultaRecenteService.listar(tenantId, 20, filialId, clienteId, "FATURADO", null, null))
                .thenReturn(List.of());

        controller.listarRecentes(20, filialId, clienteId, "FATURADO", null, null);

        verify(consultaRecenteService).listar(tenantId, 20, filialId, clienteId, "FATURADO", null, null);
        verifyNoInteractions(service, detalheConsultaService);
    }
}
