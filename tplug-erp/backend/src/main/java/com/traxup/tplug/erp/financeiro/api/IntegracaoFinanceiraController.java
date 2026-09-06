package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.integracao.IntegracaoFinanceiraApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/integracoes")
public class IntegracaoFinanceiraController {
    private final IntegracaoFinanceiraApplicationService service;
    private final TenantContext tenantContext;

    public IntegracaoFinanceiraController(IntegracaoFinanceiraApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/contas/{contaId}")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_LER')")
    public List<IntegracaoFinanceiraResponse> listar(@PathVariable UUID contaId) {
        return service.listar(tenantContext.tenantId(), contaId).stream()
                .map(IntegracaoFinanceiraResponse::from).toList();
    }

    @PostMapping("/contas/{contaId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public IntegracaoFinanceiraResponse criar(@PathVariable UUID contaId,
                                               @Valid @RequestBody CriarIntegracaoFinanceiraRequest request) {
        return IntegracaoFinanceiraResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.provedor(), request.identificadorExterno()));
    }

    @PostMapping("/{integracaoId}/desativar")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONCILIACAO_EDITAR')")
    public IntegracaoFinanceiraResponse desativar(@PathVariable UUID integracaoId) {
        return IntegracaoFinanceiraResponse.from(service.desativar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), integracaoId));
    }
}
