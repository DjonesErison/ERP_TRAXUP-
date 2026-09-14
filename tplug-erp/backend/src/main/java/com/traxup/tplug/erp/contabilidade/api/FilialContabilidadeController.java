package com.traxup.tplug.erp.contabilidade.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.contabilidade.FilialContabilidadeApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contabilidade/filiais")
public class FilialContabilidadeController {
    private final FilialContabilidadeApplicationService service;
    private final TenantContext tenantContext;

    public FilialContabilidadeController(
            FilialContabilidadeApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDADE_FILIAIS_LER')")
    public List<FilialContabilidadeApplicationService.FilialDisponivel>
            listar() {
        return service.listar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo());
    }
}
