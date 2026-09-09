package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalRejeicaoApplicationService;
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
@RequestMapping("/api/v1/fiscal")
public class FiscalRejeicaoController {
    private final FiscalRejeicaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalRejeicaoController(FiscalRejeicaoApplicationService service,
                                    TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/documentos/{documentoId}/rejeicoes")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalRejeicaoApplicationService.Resultado registrar(
            @PathVariable UUID documentoId, @RequestBody RegistroRequest request) {
        return service.registrar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(),
                documentoId, request.transmissaoId(), request.origem(), request.codigo(),
                request.mensagem(), request.categoria(), request.corrigivel(),
                request.camposCorrecao());
    }

    @GetMapping("/documentos/{documentoId}/rejeicoes")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public List<FiscalRejeicaoApplicationService.Rejeicao> listar(
            @PathVariable UUID documentoId) {
        return service.listar(tenantContext.tenantId(), documentoId);
    }

    @PostMapping("/rejeicoes/{rejeicaoId}/iniciar-correcao")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalRejeicaoApplicationService.Resultado iniciarCorrecao(
            @PathVariable UUID rejeicaoId) {
        return service.iniciarCorrecao(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), rejeicaoId);
    }

    public record RegistroRequest(UUID transmissaoId, String origem, String codigo,
                                  String mensagem, String categoria, boolean corrigivel,
                                  List<String> camposCorrecao) {}
}
