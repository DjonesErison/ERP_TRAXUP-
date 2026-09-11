package com.traxup.tplug.erp.contabilidade.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.contabilidade.SpedExportacaoApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contabilidade/sped/exportacoes")
public class SpedExportacaoController {
    private final SpedExportacaoApplicationService service;
    private final TenantContext tenantContext;

    public SpedExportacaoController(
            SpedExportacaoApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONTABILIDADE_SPED_SOLICITAR')")
    public SpedExportacaoApplicationService.Exportacao solicitar(
            @Valid @RequestBody SolicitarExportacaoRequest request) {
        return service.solicitar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                request.tipo(), request.competencia());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDADE_SPED_SOLICITAR')")
    public List<SpedExportacaoApplicationService.Exportacao> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer limite) {
        return service.listar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                tipo, limite);
    }

    public record SolicitarExportacaoRequest(
            @NotBlank String tipo,
            @NotNull YearMonth competencia) {}
}
