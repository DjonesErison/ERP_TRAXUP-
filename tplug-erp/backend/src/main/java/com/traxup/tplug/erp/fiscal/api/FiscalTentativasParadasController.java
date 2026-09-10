package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativasParadasApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativasParadasController {
    private final FiscalTentativasParadasApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativasParadasController(
            FiscalTentativasParadasApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/paradas")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalTentativasParadasApplicationService.Resultado listar(
            @RequestParam(required = false) Integer minutos) {
        return service.listar(tenantContext.tenantId(), minutos);
    }
}
