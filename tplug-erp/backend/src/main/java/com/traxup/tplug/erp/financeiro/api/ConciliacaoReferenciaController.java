package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ConciliacaoReferenciaApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/conciliacao")
public class ConciliacaoReferenciaController {
    private final ConciliacaoReferenciaApplicationService service;
    private final TenantContext tenantContext;

    public ConciliacaoReferenciaController(ConciliacaoReferenciaApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/contas/{contaId}/lancamentos/referencia")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public ConciliacaoLancamentoResponse buscar(@PathVariable UUID contaId,
                                                 @RequestParam String origem,
                                                 @RequestParam String referenciaExterna) {
        return ConciliacaoLancamentoResponse.from(service.buscar(
                tenantContext.tenantId(), contaId, origem, referenciaExterna));
    }
}
