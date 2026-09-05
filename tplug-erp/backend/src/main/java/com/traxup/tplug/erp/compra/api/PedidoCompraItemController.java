package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.compra.PedidoCompraItemApplicationService;
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
@RequestMapping("/api/v1/compras/pedidos/{pedidoId}/itens")
public class PedidoCompraItemController {

    private final PedidoCompraItemApplicationService service;
    private final TenantContext tenantContext;

    public PedidoCompraItemController(PedidoCompraItemApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPRA_PEDIDO_LER')")
    public List<PedidoCompraItemResponse> listar(@PathVariable UUID pedidoId) {
        return service.listar(tenantContext.tenantId(), pedidoId).stream()
                .map(PedidoCompraItemResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('COMPRA_PEDIDO_EDITAR')")
    public PedidoCompraItemResponse adicionar(@PathVariable UUID pedidoId,
                                               @Valid @RequestBody AdicionarPedidoCompraItemRequest request) {
        return PedidoCompraItemResponse.from(service.adicionar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                pedidoId,
                request.produtoId(),
                request.gradeId(),
                request.quantidade(),
                request.precoUnitario()));
    }
}
