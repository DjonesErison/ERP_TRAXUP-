package com.traxup.tplug.erp.filial.api;

import com.traxup.tplug.erp.filial.Filial;
import com.traxup.tplug.erp.filial.FilialApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/filiais")
public class FilialController {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final FilialApplicationService filialApplicationService;

    public FilialController(FilialApplicationService filialApplicationService) {
        this.filialApplicationService = filialApplicationService;
    }

    @GetMapping
    public List<FilialResponse> listar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @RequestParam(required = false) UUID empresaId) {
        List<Filial> filiais = empresaId == null
                ? filialApplicationService.listar(tenantId)
                : filialApplicationService.listarPorEmpresa(tenantId, empresaId);

        return filiais.stream()
                .map(filial -> FilialResponse.from(tenantId, filial))
                .toList();
    }

    @GetMapping("/{filialId}")
    public FilialResponse buscarPorId(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @PathVariable UUID filialId) {
        return FilialResponse.from(
                tenantId,
                filialApplicationService.buscarPorId(tenantId, filialId));
    }

    @PostMapping
    public ResponseEntity<FilialResponse> criar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @Valid @RequestBody CriarFilialRequest request) {
        Filial filial = filialApplicationService.criar(
                tenantId,
                request.empresaId(),
                request.nome(),
                request.cnpj());

        return ResponseEntity
                .created(URI.create("/api/v1/filiais/" + filial.getId()))
                .body(FilialResponse.from(tenantId, filial));
    }

    @PatchMapping("/{filialId}/desativar")
    public FilialResponse desativar(
            @RequestHeader(TENANT_HEADER) UUID tenantId,
            @PathVariable UUID filialId) {
        return FilialResponse.from(
                tenantId,
                filialApplicationService.desativar(tenantId, filialId));
    }
}
