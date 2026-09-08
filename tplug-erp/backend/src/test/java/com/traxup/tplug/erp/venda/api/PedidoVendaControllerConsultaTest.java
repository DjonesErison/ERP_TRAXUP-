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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(consultaRecenteService.listar(tenantId, 0, 20, filialId, clienteId, "PV-123", "FATURADO", null, null))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        controller.listarRecentes(0, 20, filialId, clienteId, "PV-123", "FATURADO", null, null);

        verify(consultaRecenteService).listar(tenantId, 0, 20, filialId, clienteId, "PV-123", "FATURADO", null, null);
        verifyNoInteractions(service, detalheConsultaService);
    }

    @Test
    void deveRetornarTotalLiquidoComUmaAgregacaoPorPagina() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        PedidoVenda pedido = mock(PedidoVenda.class);
        when(pedido.getId()).thenReturn(pedidoId);
        when(pedido.getNumero()).thenReturn("PV-500");
        when(pedido.getStatus()).thenReturn("FATURADO");
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(consultaRecenteService.listar(tenantId, 0, 20, null, null, null, null, null, null))
                .thenReturn(new PageImpl<>(List.of(pedido), PageRequest.of(0, 20), 1));
        when(detalheConsultaService.totalLiquidoPorPedidos(tenantId, List.of(pedidoId)))
                .thenReturn(Map.of(pedidoId, new BigDecimal("42.5000")));

        mockMvc.perform(get("/api/v1/vendas/pedidos/recentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].id").value(pedidoId.toString()))
                .andExpect(jsonPath("$.conteudo[0].numero").value("PV-500"))
                .andExpect(jsonPath("$.conteudo[0].totalLiquido").value(42.5));

        verify(detalheConsultaService).totalLiquidoPorPedidos(tenantId, List.of(pedidoId));
    }

    @Test
    void deveConverterPeriodoIsoEPaginacaoPelaCamadaHttp() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-09-08T10:15:30Z");
        Instant fim = Instant.parse("2026-09-08T12:45:00Z");
        PageRequest pageRequest = PageRequest.of(2, 15);
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(consultaRecenteService.listar(tenantId, 2, 15, null, null, null, null, inicio, fim))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 41));

        mockMvc.perform(get("/api/v1/vendas/pedidos/recentes")
                        .param("pagina", "2")
                        .param("tamanho", "15")
                        .param("inicio", "2026-09-08T10:15:30Z")
                        .param("fim", "2026-09-08T12:45:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(2))
                .andExpect(jsonPath("$.tamanho").value(15))
                .andExpect(jsonPath("$.totalRegistros").value(41))
                .andExpect(jsonPath("$.totalPaginas").value(3));

        verify(consultaRecenteService).listar(tenantId, 2, 15, null, null, null, null, inicio, fim);
        verifyNoInteractions(service, detalheConsultaService);
    }

    @Test
    void deveEncaminharNumeroPelaCamadaHttp() throws Exception {
        UUID tenantId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(tenantContext.tenantId()).thenReturn(tenantId);
        when(consultaRecenteService.listar(tenantId, 0, 20, null, null, "PV-123", null, null, null))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        mockMvc.perform(get("/api/v1/vendas/pedidos/recentes").param("numero", "PV-123"))
                .andExpect(status().isOk());

        verify(consultaRecenteService).listar(tenantId, 0, 20, null, null, "PV-123", null, null, null);
        verifyNoInteractions(service, detalheConsultaService);
    }

    @Test
    void deveRejeitarPeriodoHttpInvalidoSemChamarServico() throws Exception {
        mockMvc.perform(get("/api/v1/vendas/pedidos/recentes")
                        .param("inicio", "08-09-2026 10:15"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service, consultaRecenteService, detalheConsultaService, tenantContext);
    }

    @Test
    void deveRejeitarFimHttpInvalidoSemChamarServico() throws Exception {
        mockMvc.perform(get("/api/v1/vendas/pedidos/recentes")
                        .param("fim", "08-09-2026 12:45"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service, consultaRecenteService, detalheConsultaService, tenantContext);
    }
}
