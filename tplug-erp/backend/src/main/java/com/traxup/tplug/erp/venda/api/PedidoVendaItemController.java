package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.venda.PedidoVendaItemApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaItemComboSelecaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendas/pedidos/{pedidoId}/itens")
public class PedidoVendaItemController {

    private final PedidoVendaItemApplicationService service;
    private final PedidoVendaItemComboSelecaoService comboSelecaoService;
    private final TenantContext tenantContext;

    public PedidoVendaItemController(PedidoVendaItemApplicationService service,
                                     PedidoVendaItemComboSelecaoService comboSelecaoService,
                                     TenantContext tenantContext) {
        this.service = service;
        this.comboSelecaoService = comboSelecaoService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public List<PedidoVendaItemResponse> listar(@PathVariable UUID pedidoId) {
        return service.listar(tenantContext.tenantId(), pedidoId).stream()
                .map(PedidoVendaItemResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaItemResponse adicionar(@PathVariable UUID pedidoId,
                                              @Valid @RequestBody AdicionarPedidoVendaItemRequest request) {
        return PedidoVendaItemResponse.from(service.adicionar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                pedidoId,
                request.produtoId(),
                request.gradeId(),
                request.quantidade(),
                request.precoUnitario()));
    }

    @PatchMapping("/{itemId}/desconto")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaItemResponse aplicarDesconto(@PathVariable UUID pedidoId,
                                                    @PathVariable UUID itemId,
                                                    @Valid @RequestBody AplicarDescontoPedidoVendaItemRequest request) {
        return PedidoVendaItemResponse.from(service.aplicarDesconto(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                pedidoId,
                itemId,
                request.descontoValor()));
    }

    @GetMapping("/{itemId}/combo-opcoes")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public List<PedidoVendaItemComboOpcaoResponse> listarComboOpcoes(@PathVariable UUID pedidoId,
                                                                     @PathVariable UUID itemId) {
        return comboSelecaoService.listar(tenantContext.tenantId(), pedidoId, itemId).stream()
                .map(PedidoVendaItemComboOpcaoResponse::from)
                .toList();
    }

    @PutMapping("/{itemId}/combo-opcoes")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public List<PedidoVendaItemComboOpcaoResponse> configurarComboOpcoes(@PathVariable UUID pedidoId,
                                                                         @PathVariable UUID itemId,
                                                                         @Valid @RequestBody ConfigurarPedidoVendaItemComboRequest request) {
        return comboSelecaoService.configurar(
                        tenantContext.tenantId(),
                        tenantContext.usuarioIdOuNulo(),
                        pedidoId,
                        itemId,
                        request.opcaoIds()).stream()
                .map(PedidoVendaItemComboOpcaoResponse::from)
                .toList();
    }
}
