package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.compra.PedidoCompraApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compras/pedidos")
public class PedidoCompraController {
    private final PedidoCompraApplicationService service;
    private final TenantContext tenantContext;
    public PedidoCompraController(PedidoCompraApplicationService service, TenantContext tenantContext) { this.service = service; this.tenantContext = tenantContext; }

    @GetMapping @PreAuthorize("hasAuthority('COMPRA_PEDIDO_LER')")
    public List<PedidoCompraResponse> listar() { return service.listar(tenantContext.tenantId()).stream().map(PedidoCompraResponse::from).toList(); }
    @GetMapping("/{pedidoId}") @PreAuthorize("hasAuthority('COMPRA_PEDIDO_LER')")
    public PedidoCompraResponse buscar(@PathVariable UUID pedidoId) { return PedidoCompraResponse.from(service.buscar(tenantContext.tenantId(), pedidoId)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('COMPRA_PEDIDO_CRIAR')")
    public PedidoCompraResponse criar(@Valid @RequestBody CriarPedidoCompraRequest request) { return PedidoCompraResponse.from(service.criar(tenantContext.tenantId(), request.filialId(), request.fornecedorId(), request.numero(), request.observacao())); }

    @PostMapping("/{pedidoId}/abrir") @PreAuthorize("hasAuthority('COMPRA_PEDIDO_EDITAR')")
    public PedidoCompraResponse abrir(@PathVariable UUID pedidoId) { return PedidoCompraResponse.from(service.abrir(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId)); }
    @PostMapping("/{pedidoId}/cancelar") @PreAuthorize("hasAuthority('COMPRA_PEDIDO_EDITAR')")
    public PedidoCompraResponse cancelar(@PathVariable UUID pedidoId) { return PedidoCompraResponse.from(service.cancelar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId)); }
}
