package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalXmlApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalXmlController {
    private final FiscalXmlApplicationService service;
    private final TenantContext tenantContext;

    public FiscalXmlController(FiscalXmlApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{documentoId}/xml-homologacao")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalXmlResponse gerar(@PathVariable UUID documentoId) {
        var r = service.gerar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), documentoId);
        return new FiscalXmlResponse(r.xmlId(), r.documentoId(), r.versao(), r.hashSha256(), r.repetida());
    }

    public record FiscalXmlResponse(UUID xmlId, UUID documentoId, String versao,
                                    String hashSha256, boolean repetida) {}
}
