package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTransmissaoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalTransmissaoController {
    private final FiscalTransmissaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTransmissaoController(FiscalTransmissaoApplicationService service,
                                       TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{documentoId}/transmissao-homologacao-simulada")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public Response transmitir(@PathVariable UUID documentoId) {
        var r = service.transmitirHomologacao(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), documentoId);
        return new Response(r.transmissaoId(), r.solicitacaoId(), r.documentoId(),
                r.assinaturaId(), r.status(), r.codigoResposta(), r.mensagemResposta(),
                r.protocolo(), r.hashResposta(), r.repetida());
    }

    public record Response(UUID transmissaoId, UUID solicitacaoId, UUID documentoId,
                           UUID assinaturaId, String status, String codigoResposta,
                           String mensagemResposta, String protocolo, String hashResposta,
                           boolean repetida) {}
}
