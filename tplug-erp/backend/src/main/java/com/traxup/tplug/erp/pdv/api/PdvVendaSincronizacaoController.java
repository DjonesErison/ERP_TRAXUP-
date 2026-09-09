package com.traxup.tplug.erp.pdv.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.pdv.PdvVendaFechamentoApplicationService;
import com.traxup.tplug.erp.pdv.PdvVendaItemSincronizacaoApplicationService;
import com.traxup.tplug.erp.pdv.PdvVendaRascunhoApplicationService;
import com.traxup.tplug.erp.pdv.PdvVendaSincronizacaoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pdv/sincronizacoes/vendas")
public class PdvVendaSincronizacaoController {
    private final PdvVendaSincronizacaoApplicationService service;
    private final PdvVendaRascunhoApplicationService rascunhoService;
    private final PdvVendaItemSincronizacaoApplicationService itemService;
    private final PdvVendaFechamentoApplicationService fechamentoService;
    private final TenantContext tenantContext;

    public PdvVendaSincronizacaoController(PdvVendaSincronizacaoApplicationService service,
                                           PdvVendaRascunhoApplicationService rascunhoService,
                                           PdvVendaItemSincronizacaoApplicationService itemService,
                                           PdvVendaFechamentoApplicationService fechamentoService,
                                           TenantContext tenantContext) {
        this.service = service;
        this.rascunhoService = rascunhoService;
        this.itemService = itemService;
        this.fechamentoService = fechamentoService;
        this.tenantContext = tenantContext;
    }

    @PostMapping @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaSincronizacaoResponse> sincronizar(@Valid @RequestBody SincronizarPdvVendaRequest request) {
        var r = service.sincronizar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.terminalId(), request.operacaoLocalId(), request.numeroLocal(), request.checksum(), request.ocorridoEm());
        return ResponseEntity.status(r.repetida() ? HttpStatus.OK : HttpStatus.CREATED).body(PdvVendaSincronizacaoResponse.from(r.sincronizacao(), r.repetida()));
    }

    @PostMapping("/rascunho") @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaRascunhoResponse> criarRascunho(@Valid @RequestBody CriarRascunhoPdvVendaRequest request) {
        var r = rascunhoService.criarOuObter(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.terminalId(), request.operacaoLocalId(), request.numeroLocal(), request.checksum(), request.ocorridoEm(), request.clienteId(), request.observacao());
        return ResponseEntity.status(r.repetida() ? HttpStatus.OK : HttpStatus.CREATED).body(PdvVendaRascunhoResponse.from(r));
    }

    @PostMapping("/{sincronizacaoId}/itens") @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaItemSincronizacaoResponse> sincronizarItem(@PathVariable UUID sincronizacaoId,
                                                                              @Valid @RequestBody SincronizarPdvVendaItemRequest request) {
        var r = itemService.sincronizar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), sincronizacaoId, request.itemLocalId(), request.produtoId(), request.gradeId(), request.quantidade(), request.precoUnitario());
        return ResponseEntity.status(r.repetido() ? HttpStatus.OK : HttpStatus.CREATED).body(PdvVendaItemSincronizacaoResponse.from(r.item(), r.repetido()));
    }

    @PostMapping("/{sincronizacaoId}/fechamento") @PreAuthorize("hasAuthority('PDV_SINCRONIZAR')")
    public ResponseEntity<PdvVendaFechamentoResponse> fechar(@PathVariable UUID sincronizacaoId,
                                                              @Valid @RequestBody FecharPdvVendaRequest request) {
        var r = fechamentoService.fechar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), sincronizacaoId,
                request.formaPagamentoId(), request.condicaoPagamentoId());
        return ResponseEntity.status(r.repetida() ? HttpStatus.OK : HttpStatus.CREATED).body(PdvVendaFechamentoResponse.from(r));
    }
}
