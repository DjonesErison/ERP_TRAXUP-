package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalProcessadoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/documentos")
public class FiscalProcessadoController {
    private final FiscalProcessadoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalProcessadoController(FiscalProcessadoApplicationService service,
                                      TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{documentoId}/processado-homologacao-simulado")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public Response gerar(@PathVariable UUID documentoId) {
        return resposta(service.gerar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), documentoId));
    }

    @GetMapping("/{documentoId}/processado-homologacao-simulado")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public Response buscar(@PathVariable UUID documentoId) {
        return resposta(service.buscar(tenantContext.tenantId(), documentoId));
    }

    private Response resposta(FiscalProcessadoApplicationService.Resultado r) {
        return new Response(r.processadoId(), r.solicitacaoId(), r.documentoId(),
                r.transmissaoId(), r.protocolo(), r.status(), r.tipo(), r.versao(),
                r.hashSha256(), r.conteudo(), r.repetida());
    }

    public record Response(UUID processadoId, UUID solicitacaoId, UUID documentoId,
                           UUID transmissaoId, String protocolo, String status,
                           String tipo, String versao, String hashSha256,
                           String conteudo, boolean repetida) {}
}
