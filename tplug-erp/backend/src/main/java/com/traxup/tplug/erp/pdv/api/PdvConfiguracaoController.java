package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvConfiguracaoApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pdv/terminais/{terminalId}/configuracao")
public class PdvConfiguracaoController {
    private final PdvConfiguracaoApplicationService service;
    private final TenantContext tenantContext;

    public PdvConfiguracaoController(PdvConfiguracaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PDV_CONFIGURACAO_LER')")
    public PdvConfiguracaoResponse buscar(@PathVariable UUID terminalId) {
        return PdvConfiguracaoResponse.from(service.buscar(tenantContext.tenantId(), terminalId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PDV_CONFIGURACAO_GERENCIAR')")
    public PdvConfiguracaoResponse atualizar(@PathVariable UUID terminalId,
                                              @Valid @RequestBody AtualizarPdvConfiguracaoRequest request) {
        return PdvConfiguracaoResponse.from(service.atualizar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(),
                terminalId, request.exigirJustificativaCancelamento(), request.exigirAutorizacaoCancelamento(),
                request.tamanhoImpressao(), request.imprimirCaixa(), request.imprimirCozinha()));
    }
}
