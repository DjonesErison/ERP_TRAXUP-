package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.venda.PedidoVendaItemApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final TenantContext tenantContext;

    public PedidoVendaItemController(PedidoVendaItemApplicationService service, TenantContext tenantContext) {
        this.service = service;
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
}
