package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalRepositorioConsultaApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/arquivos")
public class FiscalRepositorioConsultaController {
    private final FiscalRepositorioConsultaApplicationService service;
    private final TenantContext tenantContext;

    public FiscalRepositorioConsultaController(
            FiscalRepositorioConsultaApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FISCAL_REPOSITORIO_CONTABILIDADE_LER')")
    public FiscalRepositorioConsultaApplicationService.Resultado listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limite) {
        return service.listar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                filialId, inicio, fim, status, limite);
    }
}
