package com.traxup.tplug.erp.empresa.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.empresa.EmpresaApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final EmpresaApplicationService empresaApplicationService;
    private final TenantContext tenantContext;

    public EmpresaController(
            EmpresaApplicationService empresaApplicationService,
            TenantContext tenantContext) {
        this.empresaApplicationService = empresaApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPRESA_LER')")
    public List<EmpresaResponse> listar() {
        UUID tenantId = tenantContext.tenantId();
        return empresaApplicationService.listar(tenantId).stream()
                .map(empresa -> EmpresaResponse.from(tenantId, empresa))
                .toList();
    }

    @GetMapping("/{empresaId}")
    @PreAuthorize("hasAuthority('EMPRESA_LER')")
    public EmpresaResponse buscarPorId(@PathVariable UUID empresaId) {
        UUID tenantId = tenantContext.tenantId();
        Empresa empresa = empresaApplicationService.buscarPorId(tenantId, empresaId);
        return EmpresaResponse.from(tenantId, empresa);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPRESA_CRIAR')")
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody CriarEmpresaRequest request) {
        UUID tenantId = tenantContext.tenantId();
        Empresa empresa = empresaApplicationService.criar(
                tenantId,
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj());

        return ResponseEntity
                .created(URI.create("/api/v1/empresas/" + empresa.getId()))
                .body(EmpresaResponse.from(tenantId, empresa));
    }

    @PatchMapping("/{empresaId}/desativar")
    @PreAuthorize("hasAuthority('EMPRESA_DESATIVAR')")
    public EmpresaResponse desativar(@PathVariable UUID empresaId) {
        UUID tenantId = tenantContext.tenantId();
        Empresa empresa = empresaApplicationService.desativar(tenantId, empresaId);
        return EmpresaResponse.from(tenantId, empresa);
    }
}
