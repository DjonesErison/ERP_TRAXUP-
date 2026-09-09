package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaXmlApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativaXmlController {
    private final FiscalTentativaXmlApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativaXmlController(FiscalTentativaXmlApplicationService service,
                                        TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{tentativaId}/xml-homologacao")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalTentativaXmlApplicationService.Resultado gerar(
            @PathVariable UUID tentativaId) {
        return service.gerar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), tentativaId);
    }
}
