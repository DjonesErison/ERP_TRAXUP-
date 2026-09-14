package com.traxup.tplug.erp.contabilidade.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.contabilidade.RelatoriosContabeisExportacaoApplicationService;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contabilidade/exportacoes")
public class RelatoriosContabeisExportacaoController {
    private static final MediaType CSV =
            MediaType.parseMediaType("text/csv;charset=UTF-8");
    private final RelatoriosContabeisExportacaoApplicationService service;
    private final TenantContext tenantContext;

    public RelatoriosContabeisExportacaoController(
            RelatoriosContabeisExportacaoApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/livro-caixa.csv")
    @PreAuthorize("hasAuthority('CONTABILIDADE_LIVRO_CAIXA_LER')")
    public ResponseEntity<byte[]> exportarLivroCaixa(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID filialId) {
        return resposta(service.exportarLivroCaixa(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                inicio, fim, filialId));
    }

    @GetMapping("/inventarios.csv")
    @PreAuthorize("hasAuthority('CONTABILIDADE_INVENTARIO_LER')")
    public ResponseEntity<byte[]> exportarInventarios(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID filialId) {
        return resposta(service.exportarInventarios(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                inicio, fim, filialId));
    }

    private ResponseEntity<byte[]> resposta(
            RelatoriosContabeisExportacaoApplicationService.Resultado resultado) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .contentLength(resultado.conteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resultado.nomeArquivo(),
                                        StandardCharsets.UTF_8)
                                .build().toString())
                .header("X-Content-SHA256", resultado.hashSha256())
                .header("X-Total-Registros",
                        Integer.toString(resultado.totalRegistros()))
                .body(resultado.conteudo());
    }
}
