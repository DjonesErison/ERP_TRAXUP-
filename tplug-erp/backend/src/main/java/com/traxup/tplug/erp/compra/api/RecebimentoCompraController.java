package com.traxup.tplug.erp.compra.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.compra.RecebimentoCompraApplicationService;
import com.traxup.tplug.erp.compra.RecebimentoCompraItem;
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
@RequestMapping("/api/v1/compras/recebimentos")
public class RecebimentoCompraController {
    private final RecebimentoCompraApplicationService service;
    private final TenantContext tenantContext;

    public RecebimentoCompraController(RecebimentoCompraApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPRA_RECEBIMENTO_LER')")
    public List<RecebimentoCompraResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream().map(RecebimentoCompraResponse::from).toList();
    }

    @GetMapping("/{recebimentoId}")
    @PreAuthorize("hasAuthority('COMPRA_RECEBIMENTO_LER')")
    public RecebimentoCompraResponse buscar(@PathVariable UUID recebimentoId) {
        return RecebimentoCompraResponse.from(service.buscar(tenantContext.tenantId(), recebimentoId));
    }

    @GetMapping("/{recebimentoId}/itens")
    @PreAuthorize("hasAuthority('COMPRA_RECEBIMENTO_LER')")
    public List<RecebimentoCompraItem> listarItens(@PathVariable UUID recebimentoId) {
        return service.listarItens(tenantContext.tenantId(), recebimentoId);
    }

    @PostMapping("/pedidos/{pedidoId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('COMPRA_RECEBIMENTO_REGISTRAR')")
    public RecebimentoCompraResponse registrar(@PathVariable UUID pedidoId,
                                               @Valid @RequestBody RegistrarRecebimentoCompraRequest request) {
        return RecebimentoCompraResponse.from(service.registrar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId,
                request.documento(), request.observacao(), request.quantidadesPorItem()));
    }

    @PostMapping("/{recebimentoId}/integrar-estoque")
    @PreAuthorize("hasAuthority('COMPRA_RECEBIMENTO_INTEGRAR_ESTOQUE')")
    public RecebimentoCompraResponse integrarEstoque(@PathVariable UUID recebimentoId) {
        return RecebimentoCompraResponse.from(service.integrarEstoque(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), recebimentoId));
    }
}
