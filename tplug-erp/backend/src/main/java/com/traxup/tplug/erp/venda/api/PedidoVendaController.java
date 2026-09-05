package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendas/pedidos")
public class PedidoVendaController {
    private final PedidoVendaApplicationService service;
    private final TenantContext tenantContext;

    public PedidoVendaController(PedidoVendaApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public List<PedidoVendaResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream().map(PedidoVendaResponse::from).toList();
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public PedidoVendaResponse buscar(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.buscar(tenantContext.tenantId(), pedidoId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_CRIAR')")
    public PedidoVendaResponse criar(@Valid @RequestBody CriarPedidoVendaRequest request) {
        return PedidoVendaResponse.from(service.criar(tenantContext.tenantId(), request.filialId(), request.clienteId(),
                request.numero(), request.observacao(), tenantContext.usuarioIdOuNulo()));
    }

    @PostMapping("/{pedidoId}/abrir")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaResponse abrir(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.abrir(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }

    @PostMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaResponse cancelar(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.cancelar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }
}
