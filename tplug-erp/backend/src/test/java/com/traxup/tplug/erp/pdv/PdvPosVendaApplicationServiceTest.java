package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVenda;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaConsultaRecenteService;
import com.traxup.tplug.erp.venda.PedidoVendaDetalheConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PdvPosVendaApplicationServiceTest {
    private PedidoVendaConsultaRecenteService consultaRecenteService;
    private PedidoVendaDetalheConsultaService detalheConsultaService;
    private PedidoVendaApplicationService pedidoVendaService;
    private AuditoriaApplicationService auditoria;
    private PdvPosVendaApplicationService service;

    @BeforeEach
    void setUp() {
        consultaRecenteService = mock(PedidoVendaConsultaRecenteService.class);
        detalheConsultaService = mock(PedidoVendaDetalheConsultaService.class);
        pedidoVendaService = mock(PedidoVendaApplicationService.class);
        auditoria = mock(AuditoriaApplicationService.class);
        service = new PdvPosVendaApplicationService(consultaRecenteService, detalheConsultaService, pedidoVendaService, auditoria);
    }

    @Test
    void segundaViaDeVendaFaturadaDeveAuditarSemAlterarVenda() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        PedidoVenda pedido = faturado(tenantId);
        when(detalheConsultaService.consultar(tenantId, pedido.getId()))
                .thenReturn(new PedidoVendaDetalheConsultaService.Detalhe(pedido, List.of(), List.of()));

        var resultado = service.segundaVia(tenantId, usuarioId, pedido.getId());

        assertThat(resultado.detalhe().pedido().getId()).isEqualTo(pedido.getId());
        assertThat(resultado.emitidoEm()).isNotNull();
        verify(auditoria).registrar(eq(tenantId), eq(usuarioId), isNull(), eq(pedido.getFilialId()),
                eq("REIMPRIMIR"), eq("PEDIDO_VENDA"), eq(pedido.getId()), eq("tipo=SEGUNDA_VIA"));
        verifyNoInteractions(pedidoVendaService);
    }

    @Test
    void segundaViaDeVendaNaoFaturadaDeveSerBloqueada() {
        UUID tenantId = UUID.randomUUID();
        PedidoVenda pedido = novoPedido(tenantId);
        when(detalheConsultaService.consultar(tenantId, pedido.getId()))
                .thenReturn(new PedidoVendaDetalheConsultaService.Detalhe(pedido, List.of(), List.of()));

        assertThatThrownBy(() -> service.segundaVia(tenantId, UUID.randomUUID(), pedido.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FATURADA");
        verifyNoInteractions(auditoria, pedidoVendaService);
    }

    @Test
    void cancelarDeveDelegarAoNucleoOficialDeVenda() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        PedidoVenda pedido = novoPedido(tenantId);
        when(pedidoVendaService.cancelar(tenantId, usuarioId, pedido.getId())).thenReturn(pedido);

        assertThat(service.cancelar(tenantId, usuarioId, pedido.getId())).isSameAs(pedido);
        verify(pedidoVendaService).cancelar(tenantId, usuarioId, pedido.getId());
    }

    private PedidoVenda novoPedido(UUID tenantId) {
        return new PedidoVenda(tenantId, UUID.randomUUID(), null, "PDV-" + UUID.randomUUID(), null, UUID.randomUUID());
    }

    private PedidoVenda faturado(UUID tenantId) {
        PedidoVenda pedido = novoPedido(tenantId);
        pedido.abrir();
        pedido.faturar();
        return pedido;
    }
}
