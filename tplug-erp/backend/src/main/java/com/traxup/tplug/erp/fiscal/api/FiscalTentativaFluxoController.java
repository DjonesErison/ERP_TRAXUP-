package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaFluxoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativaFluxoController {
    private final FiscalTentativaFluxoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativaFluxoController(FiscalTentativaFluxoApplicationService service,
                                          TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{tentativaId}/processar-homologacao-simulado")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalTentativaFluxoApplicationService.Resultado processar(
            @PathVariable UUID tentativaId) {
        return service.processar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), tentativaId);
    }
}
