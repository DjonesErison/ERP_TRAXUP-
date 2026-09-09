package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal")
public class FiscalTentativaController {
    private final FiscalTentativaApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativaController(FiscalTentativaApplicationService service,
                                     TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/correcoes/{correcaoId}/nova-tentativa")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalTentativaApplicationService.Resultado abrir(
            @PathVariable UUID correcaoId) {
        return service.abrir(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), correcaoId);
    }

    @GetMapping("/documentos/{documentoId}/tentativas")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public List<FiscalTentativaApplicationService.Resumo> listar(
            @PathVariable UUID documentoId) {
        return service.listar(tenantContext.tenantId(), documentoId);
    }
}
