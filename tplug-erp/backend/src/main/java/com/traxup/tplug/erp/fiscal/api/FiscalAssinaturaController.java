package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalAssinaturaApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalAssinaturaController {
    private final FiscalAssinaturaApplicationService service;
    private final TenantContext tenantContext;

    public FiscalAssinaturaController(FiscalAssinaturaApplicationService service,
                                      TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{documentoId}/assinatura-homologacao-simulada")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public Response assinar(@PathVariable UUID documentoId) {
        var r = service.assinarHomologacao(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), documentoId);
        return new Response(r.assinaturaId(), r.documentoId(), r.xmlId(), r.certificadoId(),
                r.tipo(), r.algoritmo(), r.hashSha256(), r.repetida());
    }

    public record Response(UUID assinaturaId, UUID documentoId, UUID xmlId, UUID certificadoId,
                           String tipo, String algoritmo, String hashSha256, boolean repetida) {}
}
