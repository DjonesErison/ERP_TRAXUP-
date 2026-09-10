package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalFalhasPendentesApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalFalhasPendentesController {
    private final FiscalFalhasPendentesApplicationService service;
    private final TenantContext tenantContext;

    public FiscalFalhasPendentesController(FiscalFalhasPendentesApplicationService service,
                                            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/falhas-pendentes")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalFalhasPendentesApplicationService.Resultado listar(
            @RequestParam(required = false) Integer limite) {
        return service.listar(tenantContext.tenantId(), limite);
    }
}
