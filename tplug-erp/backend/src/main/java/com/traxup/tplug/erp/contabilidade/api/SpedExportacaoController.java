package com.traxup.tplug.erp.contabilidade.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.contabilidade.SpedDownloadApplicationService;
import com.traxup.tplug.erp.contabilidade.SpedExportacaoApplicationService;
import com.traxup.tplug.erp.contabilidade.SpedReprocessamentoApplicationService;
import com.traxup.tplug.erp.contabilidade.SpedResumoApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contabilidade/sped/exportacoes")
public class SpedExportacaoController {
    private final SpedExportacaoApplicationService service;
    private final SpedDownloadApplicationService downloadService;
    private final SpedReprocessamentoApplicationService reprocessamentoService;
    private final SpedResumoApplicationService resumoService;
    private final TenantContext tenantContext;

    public SpedExportacaoController(
            SpedExportacaoApplicationService service,
            SpedDownloadApplicationService downloadService,
            SpedReprocessamentoApplicationService reprocessamentoService,
            SpedResumoApplicationService resumoService,
            TenantContext tenantContext) {
        this.service = service;
        this.downloadService = downloadService;
        this.reprocessamentoService = reprocessamentoService;
        this.resumoService = resumoService;
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
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth competenciaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth competenciaFim,
            @RequestParam(required = false) Integer limite) {
        return service.listar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                tipo, status, competenciaInicio, competenciaFim, limite);
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('CONTABILIDADE_SPED_SOLICITAR')")
    public SpedResumoApplicationService.Resumo resumir(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth competenciaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth competenciaFim) {
        return resumoService.resumir(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                tipo, competenciaInicio, competenciaFim);
    }

    @GetMapping("/{id}/arquivo")
    @PreAuthorize("hasAuthority('CONTABILIDADE_SPED_BAIXAR')")
    public ResponseEntity<byte[]> baixar(@PathVariable UUID id) {
        SpedDownloadApplicationService.Download download =
                downloadService.baixar(
                        tenantContext.tenantId(),
                        tenantContext.usuarioIdOuNulo(), id);
        String disposition = ContentDisposition.attachment()
                .filename(download.nomeArquivo(), StandardCharsets.UTF_8)
                .build().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(download.conteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(download.conteudo());
    }

    @PostMapping("/{id}/reprocessamento")
    @PreAuthorize("hasAuthority('CONTABILIDADE_SPED_REPROCESSAR')")
    public SpedReprocessamentoApplicationService.Resultado reprocessar(
            @PathVariable UUID id) {
        return reprocessamentoService.reprocessar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), id);
    }

    public record SolicitarExportacaoRequest(
            @NotBlank String tipo,
            @NotNull YearMonth competencia) {}
}
