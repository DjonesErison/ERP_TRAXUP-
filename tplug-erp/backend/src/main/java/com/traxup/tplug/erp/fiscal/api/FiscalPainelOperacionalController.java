package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalPainelOperacionalApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fiscal")
public class FiscalPainelOperacionalController {
    private final FiscalPainelOperacionalApplicationService service;
    private final TenantContext tenantContext;

    public FiscalPainelOperacionalController(
            FiscalPainelOperacionalApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/painel-operacional")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalPainelOperacionalApplicationService.Painel buscar() {
        return service.buscar(tenantContext.tenantId());
    }
}
