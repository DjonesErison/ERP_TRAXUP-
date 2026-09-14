package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.auth.FilialAcessoApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rbac/usuarios/{usuarioId}/filiais")
@PreAuthorize("hasAuthority('RBAC_GERENCIAR')")
public class RbacFilialAcessoController {
    private final FilialAcessoApplicationService service;
    private final TenantContext tenantContext;

    public RbacFilialAcessoController(
            FilialAcessoApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public FilialAcessoApplicationService.AcessoFiliais listar(
            @PathVariable UUID usuarioId) {
        return service.listar(tenantContext.tenantId(), usuarioId);
    }

    @PutMapping
    public FilialAcessoApplicationService.AcessoFiliais substituir(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody SubstituirFiliaisRequest request) {
        return service.substituir(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                usuarioId, request.filialIds());
    }

    public record SubstituirFiliaisRequest(
            @NotNull List<@NotNull UUID> filialIds) {}
}
