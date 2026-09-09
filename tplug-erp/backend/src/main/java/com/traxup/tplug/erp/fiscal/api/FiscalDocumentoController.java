package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalDocumentoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/solicitacoes")
public class FiscalDocumentoController {
    private final FiscalDocumentoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalDocumentoController(FiscalDocumentoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{solicitacaoId}/documento")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalDocumentoResponse estruturar(@PathVariable UUID solicitacaoId) {
        var r = service.estruturar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), solicitacaoId);
        return new FiscalDocumentoResponse(r.documentoId(), r.quantidadeItens(),
                r.valorBruto(), r.valorDesconto(), r.valorTotal(), r.repetida());
    }

    public record FiscalDocumentoResponse(UUID documentoId, int quantidadeItens,
                                          BigDecimal valorBruto, BigDecimal valorDesconto,
                                          BigDecimal valorTotal, boolean repetida) {}
}
