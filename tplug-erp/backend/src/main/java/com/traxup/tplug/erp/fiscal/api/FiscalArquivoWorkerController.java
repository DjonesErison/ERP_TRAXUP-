package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalArquivoWorkerApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fiscal/arquivos")
public class FiscalArquivoWorkerController {
    private final FiscalArquivoWorkerApplicationService service;
    private final TenantContext tenantContext;

    public FiscalArquivoWorkerController(FiscalArquivoWorkerApplicationService service,
                                         TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/processar-pendentes")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalArquivoWorkerApplicationService.Resultado processar(
            @RequestParam(required = false) Integer limite) {
        return service.processar(tenantContext.tenantId(), limite);
    }
}
