package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalSolicitacaoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/solicitacoes")
public class FiscalSolicitacaoController {
    private final FiscalSolicitacaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalSolicitacaoController(FiscalSolicitacaoApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public List<FiscalSolicitacaoResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream()
                .map(s -> FiscalSolicitacaoResponse.from(s, false)).toList();
    }

    @GetMapping("/{solicitacaoId}")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalSolicitacaoResponse buscar(@PathVariable UUID solicitacaoId) {
        return FiscalSolicitacaoResponse.from(service.buscar(tenantContext.tenantId(), solicitacaoId), false);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public ResponseEntity<FiscalSolicitacaoResponse> solicitar(@Valid @RequestBody CriarFiscalSolicitacaoRequest request) {
        var resultado = service.solicitar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(),
                request.pedidoVendaId(), request.modelo(), request.ambiente());
        return ResponseEntity.status(resultado.repetida() ? HttpStatus.OK : HttpStatus.CREATED)
                .body(FiscalSolicitacaoResponse.from(resultado.solicitacao(), resultado.repetida()));
    }
}
