package com.traxup.tplug.erp.contabilidade.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.contabilidade.PacoteMensalContabilidadeApplicationService;
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
import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contabilidade/pacote-mensal")
public class PacoteMensalContabilidadeController {
    private final PacoteMensalContabilidadeApplicationService service;
    private final TenantContext tenantContext;

    public PacoteMensalContabilidadeController(
            PacoteMensalContabilidadeApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("""
            hasAuthority('CONTABILIDADE_FECHAMENTO_LER')
            and hasAuthority('FISCAL_REPOSITORIO_CONTABILIDADE_LER')
            and hasAuthority('CONTABILIDADE_LIVRO_CAIXA_LER')
            and hasAuthority('CONTABILIDADE_INVENTARIO_LER')
            and hasAuthority('CONTABILIDADE_SPED_BAIXAR')
            """)
    public ResponseEntity<byte[]> gerar(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia,
            @RequestParam UUID filialId) {
        var resultado = service.gerar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                competencia, filialId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(resultado.conteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resultado.nomeArquivo(),
                                        StandardCharsets.UTF_8)
                                .build().toString())
                .header("X-Content-SHA256", resultado.hashSha256())
                .header("X-Total-Arquivos",
                        Integer.toString(resultado.totalArquivos()))
                .header("X-Checklist-Status",
                        resultado.statusChecklist())
                .body(resultado.conteudo());
    }
}
