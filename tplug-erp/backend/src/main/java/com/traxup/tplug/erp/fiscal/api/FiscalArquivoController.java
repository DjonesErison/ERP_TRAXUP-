package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalArquivoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/processados")
public class FiscalArquivoController {
    private final FiscalArquivoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalArquivoController(FiscalArquivoApplicationService service,
                                   TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{processadoId}/arquivamento")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalArquivoApplicationService.Resultado preparar(
            @PathVariable UUID processadoId) {
        return service.preparar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), processadoId);
    }

    @GetMapping("/{processadoId}/arquivamento")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalArquivoApplicationService.Resultado buscar(
            @PathVariable UUID processadoId) {
        return service.buscar(tenantContext.tenantId(), processadoId);
    }
}
