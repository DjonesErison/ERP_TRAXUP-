package com.traxup.tplug.erp.filial.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.filial.Filial;
import com.traxup.tplug.erp.filial.FilialApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/filiais")
public class FilialController {

    private final FilialApplicationService filialApplicationService;
    private final AuditoriaApplicationService auditoriaApplicationService;
    private final TenantContext tenantContext;

    public FilialController(
            FilialApplicationService filialApplicationService,
            AuditoriaApplicationService auditoriaApplicationService,
            TenantContext tenantContext) {
        this.filialApplicationService = filialApplicationService;
        this.auditoriaApplicationService = auditoriaApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FILIAL_LER')")
    public List<FilialResponse> listar(@RequestParam(required = false) UUID empresaId) {
        UUID tenantId = tenantContext.tenantId();
        List<Filial> filiais = empresaId == null
                ? filialApplicationService.listar(tenantId)
                : filialApplicationService.listarPorEmpresa(tenantId, empresaId);

        return filiais.stream()
                .map(filial -> FilialResponse.from(tenantId, filial))
                .toList();
    }

    @GetMapping("/{filialId}")
    @PreAuthorize("hasAuthority('FILIAL_LER')")
    public FilialResponse buscarPorId(@PathVariable UUID filialId) {
        UUID tenantId = tenantContext.tenantId();
        return FilialResponse.from(
                tenantId,
                filialApplicationService.buscarPorId(tenantId, filialId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FILIAL_CRIAR')")
    public ResponseEntity<FilialResponse> criar(@Valid @RequestBody CriarFilialRequest request) {
        UUID tenantId = tenantContext.tenantId();
        Filial filial = filialApplicationService.criar(
                tenantId,
                request.empresaId(),
                request.nome(),
                request.cnpj());

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                filial.getEmpresa().getId(),
                filial.getId(),
                "CRIAR",
                "FILIAL",
                filial.getId(),
                null);

        return ResponseEntity
                .created(URI.create("/api/v1/filiais/" + filial.getId()))
                .body(FilialResponse.from(tenantId, filial));
    }

    @PatchMapping("/{filialId}/desativar")
    @PreAuthorize("hasAuthority('FILIAL_DESATIVAR')")
    public FilialResponse desativar(@PathVariable UUID filialId) {
        UUID tenantId = tenantContext.tenantId();
        Filial filial = filialApplicationService.desativar(tenantId, filialId);

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                filial.getEmpresa().getId(),
                filial.getId(),
                "DESATIVAR",
                "FILIAL",
                filial.getId(),
                null);

        return FilialResponse.from(tenantId, filial);
    }
}
