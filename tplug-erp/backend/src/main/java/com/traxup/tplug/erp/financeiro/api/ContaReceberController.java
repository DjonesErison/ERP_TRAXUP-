package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.ContaReceberApplicationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    public List<ContaReceberResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoFim) {
        return service.listar(tenantContext.tenantId(), status, vencimentoInicio, vencimentoFim).stream()
                .map(ContaReceberResponse::from)
                .toList();
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public TituloFinanceiroResumoResponse resumir(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoFim) {
        return TituloFinanceiroResumoResponse.from(
                service.resumir(tenantContext.tenantId(), status, vencimentoInicio, vencimentoFim));
    }

    @GetMapping("/origens/{origemTipo}/{origemId}")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public List<ContaReceberResponse> listarPorOrigem(@PathVariable String origemTipo,
                                                       @PathVariable UUID origemId) {
        return service.listarPorOrigem(tenantContext.tenantId(), origemTipo, origemId).stream()
                .map(ContaReceberResponse::from)
                .toList();
    }

    @GetMapping("/{contaId}")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public ContaReceberResponse buscar(@PathVariable UUID contaId) {
        return ContaReceberResponse.from(service.buscar(tenantContext.tenantId(), contaId));
    }

    @GetMapping("/{contaId}/recebimentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_LER')")
    public List<ContaReceberRecebimentoResponse> listarRecebimentos(@PathVariable UUID contaId) {
        return service.listarRecebimentos(tenantContext.tenantId(), contaId).stream()
                .map(ContaReceberRecebimentoResponse::from)
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

    @PostMapping("/{contaId}/receber")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_BAIXAR')")
    public ContaReceberResponse receber(@PathVariable UUID contaId) {
        return ContaReceberResponse.from(service.receber(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }

    @PostMapping("/{contaId}/recebimentos")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_BAIXAR')")
    public ContaReceberResponse registrarRecebimento(@PathVariable UUID contaId,
                                                      @Valid @RequestBody RegistrarRecebimentoContaRequest request) {
        return ContaReceberResponse.from(service.receber(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId, request.valor()));
    }

    @PostMapping("/{contaId}/cancelar")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_CANCELAR')")
    public ContaReceberResponse cancelar(@PathVariable UUID contaId) {
        return ContaReceberResponse.from(service.cancelar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId));
    }
}
