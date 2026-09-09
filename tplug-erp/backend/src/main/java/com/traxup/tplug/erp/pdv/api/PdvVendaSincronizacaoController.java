package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvVendaProcessamentoApplicationService;
import com.traxup.tplug.erp.pdv.PdvVendaSincronizacaoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pdv/sincronizacoes/vendas")
public class PdvVendaSincronizacaoController {
    private final PdvVendaSincronizacaoApplicationService service;
    private final PdvVendaProcessamentoApplicationService processamentoService;
    private final TenantContext tenantContext;

    public PdvVendaSincronizacaoController(PdvVendaSincronizacaoApplicationService service,
                                           PdvVendaProcessamentoApplicationService processamentoService,
                                           TenantContext tenantContext) {
        this.service = service;
        this.processamentoService = processamentoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaSincronizacaoResponse> sincronizar(@Valid @RequestBody SincronizarPdvVendaRequest request) {
        var resultado = service.sincronizar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.terminalId(), request.operacaoLocalId(),
                request.numeroLocal(), request.checksum(), request.ocorridoEm());
        var response = PdvVendaSincronizacaoResponse.from(resultado.sincronizacao(), resultado.repetida());
        return ResponseEntity.status(resultado.repetida() ? HttpStatus.OK : HttpStatus.CREATED).body(response);
    }

    @PostMapping("/processar")
    @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaProcessamentoResponse> processar(@Valid @RequestBody ProcessarPdvVendaRequest request) {
        var itens = request.itens().stream()
                .map(item -> new PdvVendaProcessamentoApplicationService.ItemComando(
                        item.produtoId(), item.gradeId(), item.quantidade(), item.precoUnitario(), item.descontoValor()))
                .toList();
        var resultado = processamentoService.processar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.terminalId(), request.operacaoLocalId(),
                request.numeroLocal(), request.checksum(), request.ocorridoEm(), request.clienteId(),
                request.formaPagamentoId(), request.condicaoPagamentoId(), request.observacao(), itens);
        var response = PdvVendaProcessamentoResponse.from(resultado);
        return ResponseEntity.status(resultado.repetida() ? HttpStatus.OK : HttpStatus.CREATED).body(response);
    }
}
