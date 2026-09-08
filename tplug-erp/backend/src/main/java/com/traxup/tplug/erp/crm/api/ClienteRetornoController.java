package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.crm.ClienteRetornoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/clientes")
public class ClienteRetornoController {
    private final ClienteRetornoApplicationService service;
    private final TenantContext tenantContext;

    public ClienteRetornoController(ClienteRetornoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public List<ClienteInativoResponse> listarInativos(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false, defaultValue = "30") Integer diasInatividade,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listarInativos(tenantContext.tenantId(), filialId, diasInatividade, limite)
                .stream()
                .map(ClienteInativoResponse::from)
                .toList();
    }
}
