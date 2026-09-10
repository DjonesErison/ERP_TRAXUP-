package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalExportacaoContabilidadeApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/fiscal/arquivos")
public class FiscalExportacaoContabilidadeController {
    private final FiscalExportacaoContabilidadeApplicationService service;
    private final TenantContext tenantContext;

    public FiscalExportacaoContabilidadeController(
            FiscalExportacaoContabilidadeApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/exportacao-contabilidade")
    @PreAuthorize("hasAuthority('FISCAL_REPOSITORIO_CONTABILIDADE_LER')")
    public ResponseEntity<byte[]> exportar(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        var resultado = service.exportar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), inicio, fim);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(resultado.conteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resultado.nomeArquivo()).build().toString())
                .header("X-Content-SHA256", resultado.hashSha256())
                .header("X-Total-XML", Integer.toString(resultado.totalXml()))
                .body(resultado.conteudo());
    }
}
