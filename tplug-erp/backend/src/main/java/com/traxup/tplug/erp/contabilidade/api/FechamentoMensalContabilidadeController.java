package com.traxup.tplug.erp.contabilidade.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.contabilidade.FechamentoMensalContabilidadeApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contabilidade/fechamento-mensal")
public class FechamentoMensalContabilidadeController {
    private final FechamentoMensalContabilidadeApplicationService service;
    private final TenantContext tenantContext;

    public FechamentoMensalContabilidadeController(
            FechamentoMensalContabilidadeApplicationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDADE_FECHAMENTO_LER')")
    public FechamentoMensalContabilidadeApplicationService.Resumo consultar(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia,
            @RequestParam(required = false) UUID filialId) {
        return service.consultar(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                competencia, filialId);
    }
}
