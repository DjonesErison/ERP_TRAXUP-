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

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PedidoVendaBuscaPorNumeroControllerTest {
    @Mock PedidoVendaApplicationService service;
    @Mock PedidoVendaConsultaRecenteService consultaRecenteService;
    @Mock PedidoVendaDetalheConsultaService detalheConsultaService;
    @Mock TenantContext tenantContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new PedidoVendaController(service, consultaRecenteService, detalheConsultaService, tenantContext))
                .build();
    }

    @Test
    void deveUsarTenantDoContextoNaBuscaDiretaPorNumero() throws Exception {
        UUID tenantId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(service.buscarPorNumero(tenantId, "PV-123")).thenReturn(pedido);

        mockMvc.perform(get("/api/v1/vendas/pedidos/por-numero/PV-123"))
                .andExpect(status().isOk());

        verify(service).buscarPorNumero(tenantId, "PV-123");
        verifyNoInteractions(consultaRecenteService, detalheConsultaService);
    }
}
