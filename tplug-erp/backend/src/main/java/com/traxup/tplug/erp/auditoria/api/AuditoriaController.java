package com.traxup.tplug.erp.auditoria.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auditoria.AuditoriaEvento;
import com.traxup.tplug.erp.auth.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditorias")
public class AuditoriaController {

    private final AuditoriaApplicationService auditoriaApplicationService;
    private final TenantContext tenantContext;

    public AuditoriaController(
            AuditoriaApplicationService auditoriaApplicationService,
            TenantContext tenantContext) {
        this.auditoriaApplicationService = auditoriaApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITORIA_LER')")
    public List<AuditoriaEvento> listar() {
        return auditoriaApplicationService.listarPorTenant(tenantContext.tenantId());
    }
}
