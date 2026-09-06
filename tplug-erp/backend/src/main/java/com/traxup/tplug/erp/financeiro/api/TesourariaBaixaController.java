package com.traxup.tplug.erp.financeiro.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.TesourariaBaixaApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/tesouraria")
public class TesourariaBaixaController {
    private final TesourariaBaixaApplicationService service;
    private final TenantContext tenantContext;

    public TesourariaBaixaController(TesourariaBaixaApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/contas-receber/{contaId}/receber")
    @PreAuthorize("hasAuthority('FINANCEIRO_RECEBER_BAIXAR') and hasAuthority('FINANCEIRO_CONTA_MOVIMENTAR')")
    public ContaReceberResponse receber(@PathVariable UUID contaId,
                                        @Valid @RequestBody ReceberEmContaRequest request) {
        return ContaReceberResponse.from(service.receberEmConta(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.contaFinanceiraId(), request.valor()));
    }

    @PostMapping("/contas-pagar/{contaId}/pagar")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR_BAIXAR') and hasAuthority('FINANCEIRO_CONTA_MOVIMENTAR')")
    public ContaPagarResponse pagar(@PathVariable UUID contaId,
                                    @Valid @RequestBody PagarEmContaRequest request) {
        if (request.valor() == null) {
            return ContaPagarResponse.from(service.pagarEmConta(
                    tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                    request.contaFinanceiraId()));
        }
        return ContaPagarResponse.from(service.pagarEmConta(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), contaId,
                request.contaFinanceiraId(), request.valor()));
    }

    public record ReceberEmContaRequest(
            @NotNull UUID contaFinanceiraId,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal valor
    ) {}

    public record PagarEmContaRequest(
            @NotNull UUID contaFinanceiraId,
            @DecimalMin(value = "0.0001") BigDecimal valor
    ) {}
}
