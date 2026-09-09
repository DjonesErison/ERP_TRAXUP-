package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalValidacaoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalValidacaoController {
    private final FiscalValidacaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalValidacaoController(FiscalValidacaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{documentoId}/validacao")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalValidacaoResponse validar(@PathVariable UUID documentoId) {
        var r = service.validar(tenantContext.tenantId(), documentoId);
        return new FiscalValidacaoResponse(r.apto(), r.quantidadeItens(),
                r.pendencias().stream().map(p -> new PendenciaResponse(p.codigo(), p.ocorrencias())).toList());
    }

    public record FiscalValidacaoResponse(boolean apto, int quantidadeItens,
                                          List<PendenciaResponse> pendencias) {}
    public record PendenciaResponse(String codigo, int ocorrencias) {}
}
