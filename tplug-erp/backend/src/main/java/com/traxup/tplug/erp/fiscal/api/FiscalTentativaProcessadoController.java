package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalTentativaProcessadoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/tentativas")
public class FiscalTentativaProcessadoController {
    private final FiscalTentativaProcessadoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalTentativaProcessadoController(
            FiscalTentativaProcessadoApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{tentativaId}/processado-homologacao-simulado")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public Response gerar(@PathVariable UUID tentativaId) {
        return resposta(service.gerar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), tentativaId));
    }

    @GetMapping("/{tentativaId}/processado-homologacao-simulado")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public Response buscar(@PathVariable UUID tentativaId) {
        return resposta(service.buscar(tenantContext.tenantId(), tentativaId));
    }

    private Response resposta(FiscalTentativaProcessadoApplicationService.Resultado r) {
        return new Response(r.processadoId(), r.documentoId(), r.tentativaId(),
                r.tentativaNumero(), r.transmissaoId(), r.protocolo(), r.status(),
                r.tipo(), r.versao(), r.hashSha256(), r.conteudo(), r.repetida());
    }

    public record Response(UUID processadoId, UUID documentoId, UUID tentativaId,
                           int tentativaNumero, UUID transmissaoId, String protocolo,
                           String status, String tipo, String versao, String hashSha256,
                           String conteudo, boolean repetida) {}
}
