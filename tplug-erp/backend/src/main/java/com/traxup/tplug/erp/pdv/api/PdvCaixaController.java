package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvCaixaApplicationService;
import com.traxup.tplug.erp.pdv.PdvCaixaSessao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pdv/caixas")
public class PdvCaixaController {
    private final PdvCaixaApplicationService service;
    private final TenantContext tenantContext;
    public PdvCaixaController(PdvCaixaApplicationService service, TenantContext tenantContext) { this.service = service; this.tenantContext = tenantContext; }

    @GetMapping("/terminal/{terminalId}")
    @PreAuthorize("hasAuthority('PDV_CAIXA_LER')")
    public List<Response> listar(@PathVariable UUID terminalId) { return service.listar(tenantContext.tenantId(), terminalId).stream().map(Response::from).toList(); }

    @GetMapping("/terminal/{terminalId}/aberta")
    @PreAuthorize("hasAuthority('PDV_CAIXA_LER')")
    public Response aberta(@PathVariable UUID terminalId) { return Response.from(service.buscarAberta(tenantContext.tenantId(), terminalId)); }

    @PostMapping("/abrir")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PDV_CAIXA_OPERAR')")
    public Response abrir(@Valid @RequestBody AbrirRequest r) { return Response.from(service.abrir(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), r.terminalId(), r.contaFinanceiraId())); }

    @PostMapping("/{sessaoId}/fechar")
    @PreAuthorize("hasAuthority('PDV_CAIXA_OPERAR')")
    public Response fechar(@PathVariable UUID sessaoId, @Valid @RequestBody FecharRequest r) { return Response.from(service.fechar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), sessaoId, r.saldoInformado(), r.observacao())); }

    public record AbrirRequest(@NotNull UUID terminalId, @NotNull UUID contaFinanceiraId) {}
    public record FecharRequest(@NotNull @PositiveOrZero BigDecimal saldoInformado, String observacao) {}
    public record Response(UUID id, UUID terminalId, UUID contaFinanceiraId, String status, BigDecimal saldoAbertura,
                           BigDecimal saldoSistemaFechamento, BigDecimal saldoInformadoFechamento, BigDecimal diferencaFechamento,
                           Instant abertoEm, Instant fechadoEm, String observacaoFechamento) {
        static Response from(PdvCaixaSessao s){ return new Response(s.getId(), s.getTerminalId(), s.getContaFinanceiraId(), s.getStatus(), s.getSaldoAbertura(), s.getSaldoSistemaFechamento(), s.getSaldoInformadoFechamento(), s.getDiferencaFechamento(), s.getAbertoEm(), s.getFechadoEm(), s.getObservacaoFechamento()); }
    }
}
