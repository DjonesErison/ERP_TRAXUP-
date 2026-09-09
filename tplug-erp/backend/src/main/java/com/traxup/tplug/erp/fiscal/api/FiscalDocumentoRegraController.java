package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalDocumentoRegraApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentdocumentos")
public class FiscalDocumentoRegraController {
    private final FiscalDocumentoRegraApplicationService service;
    private final TenantContext tenantContext;

    public FiscalDocumentoRegraController(FiscalDocumentoRegraApplicationService service,
                                          TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{documentoId}/regra-operacao")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public RegraAplicadaResponse aplicar(@PathVariable UUID documentoId,
                                         @Valid @RequestBody AplicarRegraRequest request) {
        var r = service.aplicar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(),
                documentoId, request.tipoOperacao(), request.regimeTributario(), request.ufDestino());
        return new RegraAplicadaResponse(r.documentoId(), r.regraId(), r.tipoOperacao(),
                r.regimeTributario(), r.ufDestino(), r.cfop(), r.cstIcms(), r.csosn(), r.repetida());
    }

    public record AplicarRegraRequest(@NotBlank String tipoOperacao,
                                      @NotBlank String regimeTributario,
                                      @NotBlank String ufDestino) {}
    public record RegraAplicadaResponse(UUID documentoId, UUID regraId, String tipoOperacao,
                                        String regimeTributario, String ufDestino, String cfop,
                                        String cstIcms, String csosn, boolean repetida) {}
}
