package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalNumeracaoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalNumeracaoController {
    private final FiscalNumeracaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalNumeracaoController(FiscalNumeracaoApplicationService service,
                                     TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{documentoId}/numeracao")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalNumeracaoResponse numerar(@PathVariable UUID documentoId) {
        var resultado = service.numerar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), documentoId);
        return new FiscalNumeracaoResponse(
                resultado.documentoId(), resultado.modelo(), resultado.ambiente(),
                resultado.serie(), resultado.numero(), resultado.numeradoEm(),
                resultado.repetida());
    }

    public record FiscalNumeracaoResponse(
            UUID documentoId,
            String modelo,
            String ambiente,
            int serie,
            long numero,
            Instant numeradoEm,
            boolean repetida
    ) {}
}
