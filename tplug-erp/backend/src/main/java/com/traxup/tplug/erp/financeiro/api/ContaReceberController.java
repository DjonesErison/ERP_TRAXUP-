package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ContaReceberApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/contas-receber")
public class ContaReceberController {
    private final ContaReceberApplicationService service;
    private final TenantContext tenantContext;

    public ContaReceberController(ContaReceberApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public List<ContaReceberResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream().map(ContaReceberResponse::from).toList();
    }

    @GetMapping("/{contaId}")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public ContaReceberResponse buscar(@PathVariable UUID contaId) {
        return ContaReceberResponse.from(service.buscar(tenantContext.tenantId(), contaId));
    }

    @GetMapping("/{contaId}/movimentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public List<ContaReceberMovimentoResponse> listarMovimentos(@PathVariable UUID contaId) {
        return service.listarMovimentos(tenantContext.tenantId(), contaId).stream()
                .map(ContaReceberMovimentoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_CRIAR')")
    public ContaReceberResponse criar(@Valid @RequestBody CriarContaReceberRequest request) {
        return ContaReceberResponse.from(service.criar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.filialId(), request.clienteId(),
                request.numeroDocumento(), request.descricao(), request.valorOriginal(), request.vencimento()));
    }

    @PostMapping("/{contaId}/recebimentos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_BAIXAR')")
    public ContaReceberMovimentoResponse registrarRecebimento(
            @PathVariable UUID contaId, @Valid @RequestBody RegistrarRecebimentoRequest request) {
        return ContaReceberMovimentoResponse.from(service.registrarRecebimento(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.valor(), request.dataRecebimento(), request.observacao()));
    }

    @PostMapping("/{contaId}/receber")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_BAIXAR')")
    public ContaReceberResponse receber(@PathVariable UUID contaId) {
        return ContaReceberResponse.from(service.receber(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }

    @PostMapping("/{contaId}/cancelar")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_CANCELAR')")
    public ContaReceberResponse cancelar(@PathVariable UUID contaId) {
        return ContaReceberResponse.from(service.cancelar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }
}
