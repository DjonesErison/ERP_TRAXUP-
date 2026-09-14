package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.LivroCaixaContabilidadeApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contabilidade/livro-caixa")
public class LivroCaixaContabilidadeController {
    private final LivroCaixaContabilidadeApplicationService service;
    private final TenantContext tenantContext;

    public LivroCaixaContabilidadeController(
            LivroCaixaContabilidadeApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDADE_LIVRO_CAIXA_LER')")
    public LivroCaixaContabilidadeApplicationService.Resultado consultar(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID filialId,
            @RequestParam(required = false) Integer limite,
            @RequestParam(defaultValue = "1") int pagina) {
        return service.consultar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                inicio, fim, filialId, limite, pagina);
    }
}
