package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.crm.ClienteRfvApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/clientes")
public class ClienteRfvController {
    private final ClienteRfvApplicationService service;
    private final TenantContext tenantContext;

    public ClienteRfvController(ClienteRfvApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/rfv")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public List<ClienteRfvResponse> listar(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listar(tenantContext.tenantId(), filialId, inicio, fim, limite)
                .stream().map(ClienteRfvResponse::from).toList();
    }
}
