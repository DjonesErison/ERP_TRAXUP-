package com.traxup.tplug.erp.inventario.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.inventario.InventarioLeituraApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventarios/itens")
public class InventarioLeituraController {
    private final InventarioLeituraApplicationService service;
    private final TenantContext tenantContext;

    public InventarioLeituraController(InventarioLeituraApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/por-codigo-barras")
    @PreAuthorize("hasAuthority('INVENTARIO_LER')")
    public InventarioItemLeituraResponse localizarPorCodigoBarra(@RequestParam String codigo) {
        return InventarioItemLeituraResponse.from(service.localizarPorCodigoBarra(tenantContext.tenantId(), codigo));
    }
}
