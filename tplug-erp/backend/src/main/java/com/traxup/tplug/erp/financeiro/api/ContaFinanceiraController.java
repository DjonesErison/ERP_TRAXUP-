package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ContaFinanceiraApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/contas-financeiras")
public class ContaFinanceiraController {
    private final ContaFinanceiraApplicationService service;
    private final TenantContext tenantContext;

    public ContaFinanceiraController(ContaFinanceiraApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_LER')")
    public List<ContaFinanceiraResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream().map(ContaFinanceiraResponse::from).toList();
    }

    @GetMapping("/{contaId}")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_LER')")
    public ContaFinanceiraResponse buscar(@PathVariable UUID contaId) {
        return ContaFinanceiraResponse.from(service.buscar(tenantContext.tenantId(), contaId));
    }

    @GetMapping("/{contaId}/movimentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_LER')")
    public List<ContaFinanceiraMovimentoResponse> movimentos(@PathVariable UUID contaId) {
        return service.listarMovimentos(tenantContext.tenantId(), contaId).stream()
                .map(ContaFinanceiraMovimentoResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_CRIAR')")
    public ContaFinanceiraResponse criar(@Valid @RequestBody CriarContaFinanceiraRequest request) {
        return ContaFinanceiraResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(), request.nome(), request.tipo()));
    }

    @PostMapping("/{contaId}/movimentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_MOVIMENTAR')")
    public ContaFinanceiraResponse movimentar(@PathVariable UUID contaId,
                                               @Valid @RequestBody MovimentarContaFinanceiraRequest request) {
        return ContaFinanceiraResponse.from(service.movimentar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.tipo(), request.valor(), request.descricao()));
    }

    @PostMapping("/{contaId}/desativar")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_DESATIVAR')")
    public ContaFinanceiraResponse desativar(@PathVariable UUID contaId) {
        return ContaFinanceiraResponse.from(service.desativar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }
}
