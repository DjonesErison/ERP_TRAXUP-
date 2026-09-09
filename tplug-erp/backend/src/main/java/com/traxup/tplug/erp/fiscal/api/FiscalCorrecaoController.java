package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalCorrecaoApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal")
public class FiscalCorrecaoController {
    private final FiscalCorrecaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalCorrecaoController(FiscalCorrecaoApplicationService service,
                                    TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/rejeicoes/{rejeicaoId}/correcoes")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalCorrecaoApplicationService.Correcao registrar(
            @PathVariable UUID rejeicaoId, @RequestBody RegistroRequest request) {
        return service.registrar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), rejeicaoId,
                request.valores(), request.motivo());
    }

    @GetMapping("/rejeicoes/{rejeicaoId}/correcoes")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public List<FiscalCorrecaoApplicationService.Correcao> listar(
            @PathVariable UUID rejeicaoId) {
        return service.listar(tenantContext.tenantId(), rejeicaoId);
    }

    @PostMapping("/correcoes/{correcaoId}/aplicar")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalCorrecaoApplicationService.Correcao aplicar(
            @PathVariable UUID correcaoId) {
        return service.aplicar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), correcaoId);
    }

    public record RegistroRequest(Map<String, Object> valores, String motivo) {}
}
