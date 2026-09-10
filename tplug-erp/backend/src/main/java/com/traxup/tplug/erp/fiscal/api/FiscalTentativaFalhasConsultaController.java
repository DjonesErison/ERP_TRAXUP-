package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaFalhasConsultaApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativaFalhasConsultaController {
    private final FiscalTentativaFalhasConsultaApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativaFalhasConsultaController(
            FiscalTentativaFalhasConsultaApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{tentativaId}/falhas")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalTentativaFalhasConsultaApplicationService.Historico buscar(
            @PathVariable UUID tentativaId) {
        return service.buscar(tenantContext.tenantId(), tentativaId);
    }
}
