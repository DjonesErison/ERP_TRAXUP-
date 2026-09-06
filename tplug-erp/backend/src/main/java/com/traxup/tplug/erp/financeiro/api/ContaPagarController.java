package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ContaPagarApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/contas-pagar")
public class ContaPagarController {
    private final ContaPagarApplicationService service;
    private final TenantContext tenantContext;

    public ContaPagarController(ContaPagarApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_LER')")
    public List<ContaPagarResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream().map(ContaPagarResponse::from).toList();
    }

    @GetMapping("/{contaId}")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_LER')")
    public ContaPagarResponse buscar(@PathVariable UUID contaId) {
        return ContaPagarResponse.from(service.buscar(tenantContext.tenantId(), contaId));
    }

    @GetMapping("/{contaId}/pagamentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_LER')")
    public List<ContaPagarPagamentoResponse> listarPagamentos(@PathVariable UUID contaId) {
        return service.listarPagamentos(tenantContext.tenantId(), contaId).stream()
                .map(ContaPagarPagamentoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_CRIAR')")
    public ContaPagarResponse criar(@Valid @RequestBody CriarContaPagarRequest request) {
        return ContaPagarResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(), request.fornecedorId(),
                request.numeroDocumento(), request.descricao(), request.valorOriginal(), request.vencimento()));
    }

    @PostMapping("/{contaId}/pagar")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_BAIXAR')")
    public ContaPagarResponse pagar(@PathVariable UUID contaId) {
        return ContaPagarResponse.from(service.pagar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }

    @PostMapping("/{contaId}/pagamentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_BAIXAR')")
    public ContaPagarResponse registrarPagamento(@PathVariable UUID contaId,
                                                  @Valid @RequestBody RegistrarPagamentoContaRequest request) {
        return ContaPagarResponse.from(service.pagar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId, request.valor()));
    }

    @PostMapping("/{contaId}/cancelar")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_CANCELAR')")
    public ContaPagarResponse cancelar(@PathVariable UUID contaId) {
        return ContaPagarResponse.from(service.cancelar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }
}
