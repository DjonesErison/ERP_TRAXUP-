package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.inventario.InventarioContabilidadeApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contabilidade/inventarios")
public class InventarioContabilidadeController {
    private final InventarioContabilidadeApplicationService service;
    private final TenantContext tenantContext;

    public InventarioContabilidadeController(
            InventarioContabilidadeApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDADE_INVENTARIO_LER')")
    public InventarioContabilidadeApplicationService.Resultado consultar(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) Integer limite,
            @RequestParam(defaultValue = "1") int pagina) {
        return service.consultar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                inicio, fim, filialId, limite, pagina);
    }
}
