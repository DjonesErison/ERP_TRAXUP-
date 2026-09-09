package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvVendaRascunhoApplicationService;
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
    private final PdvVendaRascunhoApplicationService rascunhoService;
    private final TenantContext tenantContext;

    public PdvVendaSincronizacaoController(PdvVendaSincronizacaoApplicationService service,
                                           PdvVendaRascunhoApplicationService rascunhoService,
                                           TenantContext tenantContext) {
        this.service = service;
        this.rascunhoService = rascunhoService;
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

    @PostMapping("/rascunho")
    @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaRascunhoResponse> criarRascunho(@Valid @RequestBody CriarRascunhoPdvVendaRequest request) {
        var resultado = rascunhoService.criarOuObter(
                tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(),
                request.terminalId(),
                request.operacaoLocalId(),
                request.numeroLocal(),
                request.checksum(),
                request.ocorridoEm(),
                request.clienteId(),
                request.observacao());
        var response = PdvVendaRascunhoResponse.from(resultado);
        return ResponseEntity.status(resultado.repetida() ? HttpStatus.OK : HttpStatus.CREATED).body(response);
    }
}
