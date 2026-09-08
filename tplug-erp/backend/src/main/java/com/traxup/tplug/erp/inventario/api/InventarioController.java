package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.inventario.InventarioApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventarios")
public class InventarioController {
    private final InventarioApplicationService service;
    private final TenantContext tenantContext;

    public InventarioController(InventarioApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_LER')")
    public List<InventarioSessaoResponse> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listar(tenantContext.tenantId(), filialId, status, limite)
                .stream().map(InventarioSessaoResponse::from).toList();
    }

    @GetMapping("/{inventarioId}")
    @PreAuthorize("hasAuthority('INVENTARIO_LER')")
    public InventarioSessaoResponse buscar(@PathVariable UUID inventarioId) {
        return InventarioSessaoResponse.from(service.buscar(tenantContext.tenantId(), inventarioId));
    }

    @GetMapping("/{inventarioId}/contagens")
    @PreAuthorize("hasAuthority('INVENTARIO_LER')")
    public List<InventarioContagemResponse> listarContagens(
            @PathVariable UUID inventarioId,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listarContagens(tenantContext.tenantId(), inventarioId, limite)
                .stream().map(InventarioContagemResponse::from).toList();
    }

    @GetMapping("/{inventarioId}/divergencias")
    @PreAuthorize("hasAuthority('INVENTARIO_LER')")
    public List<InventarioContagemResponse> listarDivergencias(
            @PathVariable UUID inventarioId,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listarDivergencias(tenantContext.tenantId(), inventarioId, limite)
                .stream().map(InventarioContagemResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INVENTARIO_EDITAR')")
    public InventarioSessaoResponse criar(@Valid @RequestBody CriarInventarioRequest request) {
        return InventarioSessaoResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(), request.descricao()));
    }

    @PostMapping("/{inventarioId}/contagens")
    @PreAuthorize("hasAuthority('INVENTARIO_EDITAR')")
    public InventarioContagemResponse registrarContagem(
            @PathVariable UUID inventarioId,
            @Valid @RequestBody RegistrarInventarioContagemRequest request) {
        return InventarioContagemResponse.from(service.registrarContagem(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), inventarioId,
                request.tipoItem(), request.itemId(), request.quantidadeContada()));
    }

    @PostMapping("/{inventarioId}/concluir")
    @PreAuthorize("hasAuthority('INVENTARIO_EDITAR')")
    public InventarioSessaoResponse concluir(@PathVariable UUID inventarioId) {
        return InventarioSessaoResponse.from(service.concluir(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), inventarioId));
    }

    @PostMapping("/{inventarioId}/ajustar-estoque")
    @PreAuthorize("hasAuthority('INVENTARIO_AJUSTAR')")
    public InventarioSessaoResponse ajustarEstoque(@PathVariable UUID inventarioId) {
        return InventarioSessaoResponse.from(service.ajustarEstoque(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), inventarioId));
    }

    @PostMapping("/{inventarioId}/cancelar")
    @PreAuthorize("hasAuthority('INVENTARIO_EDITAR')")
    public InventarioSessaoResponse cancelar(@PathVariable UUID inventarioId) {
        return InventarioSessaoResponse.from(service.cancelar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), inventarioId));
    }
}
