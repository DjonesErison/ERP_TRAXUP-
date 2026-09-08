package com.traxup.tplug.erp.crm.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.crm.ClienteSegmentacaoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/segmentacoes")
public class ClienteSegmentacaoController {
    private final ClienteSegmentacaoApplicationService service;
    private final TenantContext tenantContext;

    public ClienteSegmentacaoController(ClienteSegmentacaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/rfm")
    @PreAuthorize("hasAuthority('CRM_CLIENTE_RETORNO_LER')")
    public List<ClienteRfmResponse> listarRfm(
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false, defaultValue = "365") Integer diasHistorico,
            @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return service.listarRfm(tenantContext.tenantId(), filialId, diasHistorico, limite)
                .stream()
                .map(ClienteRfmResponse::from)
                .toList();
    }
}
