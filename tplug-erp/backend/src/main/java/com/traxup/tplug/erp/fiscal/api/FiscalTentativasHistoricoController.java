package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativasHistoricoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalTentativasHistoricoController {
    private final FiscalTentativasHistoricoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativasHistoricoController(
            FiscalTentativasHistoricoApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{documentoId}/historico-tentativas")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalTentativasHistoricoApplicationService.Historico buscar(
            @PathVariable UUID documentoId) {
        return service.buscar(tenantContext.tenantId(), documentoId);
    }
}
