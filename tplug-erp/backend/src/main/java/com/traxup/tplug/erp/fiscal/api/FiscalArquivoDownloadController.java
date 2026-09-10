package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalArquivoDownloadApplicationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/arquivos")
public class FiscalArquivoDownloadController {
    private final FiscalArquivoDownloadApplicationService service;
    private final TenantContext tenantContext;

    public FiscalArquivoDownloadController(
            FiscalArquivoDownloadApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{arquivoId}/download")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public ResponseEntity<byte[]> baixar(@PathVariable UUID arquivoId) {
        var resultado = service.baixar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), arquivoId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .contentLength(resultado.conteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="" + resultado.nomeArquivo() + """)
                .header("X-Content-SHA256", resultado.hashSha256())
                .body(resultado.conteudo());
    }
}
