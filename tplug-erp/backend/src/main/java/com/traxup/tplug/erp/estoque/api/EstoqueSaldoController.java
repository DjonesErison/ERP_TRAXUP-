package com.traxup.tplug.erp.estoque.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.estoque.EstoqueSaldoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estoque/saldos")
public class EstoqueSaldoController {

    private final EstoqueSaldoApplicationService estoqueSaldoApplicationService;
    private final TenantContext tenantContext;

    public EstoqueSaldoController(
            EstoqueSaldoApplicationService estoqueSaldoApplicationService,
            TenantContext tenantContext) {
        this.estoqueSaldoApplicationService = estoqueSaldoApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/filiais/{filialId}")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public List<EstoqueSaldoResponse> listarPorFilial(@PathVariable UUID filialId) {
        UUID tenantId = tenantContext.tenantId();
        return estoqueSaldoApplicationService.listarPorFilial(tenantId, filialId).stream()
                .map(EstoqueSaldoResponse::from)
                .toList();
    }
}
