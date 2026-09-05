package com.traxup.tplug.erp.empresa.api;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.empresa.EmpresaApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final EmpresaApplicationService empresaApplicationService;

    public EmpresaController(EmpresaApplicationService empresaApplicationService) {
        this.empresaApplicationService = empresaApplicationService;
    }

    @GetMapping
    public List<EmpresaResponse> listar(@RequestHeader(TENANT_HEADER) UUID tenantId) {
        return empresaApplicationService.listar(tenantId).stream()
                .map(empresa -> EmpresaResponse.from(tenantId, empresa))
                .toList();
    }

    @GetMapping("/{empresaId}")
    public EmpresaResponse buscarPorId(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @PathVariable UUID empresaId) {
        Empresa empresa = empresaApplicationService.buscarPorId(tenantId, empresaId);
        return EmpresaResponse.from(tenantId, empresa);
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> criar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @Valid @RequestBody CriarEmpresaRequest request) {
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
    public EmpresaResponse desativar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @PathVariable UUID empresaId) {
        Empresa empresa = empresaApplicationService.desativar(tenantId, empresaId);
        return EmpresaResponse.from(tenantId, empresa);
    }
}
