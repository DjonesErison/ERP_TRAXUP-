package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalPerfilFilialApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/perfis-filial")
public class FiscalPerfilFilialController {
    private final FiscalPerfilFilialApplicationService service;
    private final TenantContext tenantContext;

    public FiscalPerfilFilialController(FiscalPerfilFilialApplicationService service,
                                        TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{filialId}")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalPerfilFilialResponse buscar(@PathVariable UUID filialId) {
        return FiscalPerfilFilialResponse.from(service.buscar(tenantContext.tenantId(), filialId));
    }

    @PutMapping("/{filialId}")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalPerfilFilialResponse salvar(@PathVariable UUID filialId,
                                             @Valid @RequestBody SalvarFiscalPerfilFilialRequest request) {
        return FiscalPerfilFilialResponse.from(service.salvar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                filialId,
                request.regimeTributario(),
                request.crt(),
                request.ambiente(),
                request.serieNfe(),
                request.serieNfce()));
    }
}
