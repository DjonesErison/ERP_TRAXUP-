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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PedidoVendaControllerConsultaTest {
    @Mock PedidoVendaApplicationService service;
    @Mock PedidoVendaConsultaRecenteService consultaRecenteService;
    @Mock PedidoVendaDetalheConsultaService detalheConsultaService;
    @Mock TenantContext tenantContext;

    private PedidoVendaController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new PedidoVendaController(service, consultaRecenteService, detalheConsultaService, tenantContext);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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

    @Test
    void deveConverterPeriodoIsoPelaCamadaHttpEEncaminharComTenantDoContexto() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-09-08T10:15:30Z");
        Instant fim = Instant.parse("2026-09-08T12:45:00Z");
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(consultaRecenteService.listar(tenantId, 20, null, null, null, inicio, fim))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/vendas/pedidos/recentes")
                        .param("inicio", "2026-09-08T10:15:30Z")
                        .param("fim", "2026-09-08T12:45:00Z"))
                .andExpect(status().isOk());

        verify(consultaRecenteService).listar(tenantId, 20, null, null, null, inicio, fim);
        verifyNoInteractions(service, detalheConsultaService);
    }
}
