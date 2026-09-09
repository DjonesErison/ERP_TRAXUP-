package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvPosVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaDetalheConsultaService;
import com.traxup.tplug.erp.venda.api.ConfigurarPagamentoPedidoVendaRequest;
import com.traxup.tplug.erp.venda.api.PedidoVendaDetalheResponse;
import com.traxup.tplug.erp.venda.api.PedidoVendaPaginaResponse;
import com.traxup.tplug.erp.venda.api.PedidoVendaResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pdv/vendas")
public class PdvPosVendaController {
    private final PdvPosVendaApplicationService service;
    private final PedidoVendaDetalheConsultaService detalheConsultaService;
    private final TenantContext tenantContext;

    public PdvPosVendaController(PdvPosVendaApplicationService service,
                                 PedidoVendaDetalheConsultaService detalheConsultaService,
                                 TenantContext tenantContext) {
        this.service = service;
        this.detalheConsultaService = detalheConsultaService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAuthority('PDV_VENDA_LER')")
    public PedidoVendaPaginaResponse recentes(@RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "20") int tamanho,
                                               @RequestParam(required = false) UUID filialId,
                                               @RequestParam(required = false) String status) {
        UUID tenantId = tenantContext.tenantId();
        var pedidos = service.listarRecentes(tenantId, pagina, tamanho, filialId, status);
        var ids = pedidos.getContent().stream().map(com.traxup.tplug.erp.venda.PedidoVenda::getId).toList();
        Map<UUID, java.math.BigDecimal> totais = ids.isEmpty() ? Map.of() : detalheConsultaService.totalLiquidoPorPedidos(tenantId, ids);
        return PedidoVendaPaginaResponse.from(pedidos, totais);
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("hasAuthority('PDV_VENDA_LER')")
    public PedidoVendaDetalheResponse detalhe(@PathVariable UUID pedidoId) {
        var detalhe = service.detalhe(tenantContext.tenantId(), pedidoId);
        return PedidoVendaDetalheResponse.from(detalhe.pedido(), detalhe.itens(), detalhe.comboOpcoes());
    }

    @PostMapping("/{pedidoId}/segunda-via")
    @PreAuthorize("hasAuthority('PDV_VENDA_REIMPRIMIR')")
    public PdvVendaComprovanteResponse segundaVia(@PathVariable UUID pedidoId) {
        return PdvVendaComprovanteResponse.from(service.segundaVia(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }

    @PostMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasAuthority('PDV_VENDA_CANCELAR')")
    public PedidoVendaResponse cancelar(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.cancelar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }

    @PostMapping("/{pedidoId}/pagamento")
    @PreAuthorize("hasAuthority('PDV_VENDA_ALTERAR_PAGAMENTO')")
    public PedidoVendaResponse alterarPagamento(@PathVariable UUID pedidoId,
                                                 @Valid @RequestBody ConfigurarPagamentoPedidoVendaRequest request) {
        return PedidoVendaResponse.from(service.alterarPagamento(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId,
                request.formaPagamentoId(), request.condicaoPagamentoId()));
    }
}
