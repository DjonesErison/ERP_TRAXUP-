package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaAssinaturaApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativaAssinaturaController {
    private final FiscalTentativaAssinaturaApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativaAssinaturaController(
            FiscalTentativaAssinaturaApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{tentativaId}/assinatura-homologacao-simulada")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalTentativaAssinaturaApplicationService.Resultado assinar(
            @PathVariable UUID tentativaId) {
        return service.assinar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), tentativaId);
    }
}
