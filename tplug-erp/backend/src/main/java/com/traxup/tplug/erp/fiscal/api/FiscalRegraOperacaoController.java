package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalRegraOperacaoApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/regras-operacao")
public class FiscalRegraOperacaoController {
    private final FiscalRegraOperacaoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalRegraOperacaoController(FiscalRegraOperacaoApplicationService service,
                                         TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public RegraResponse salvar(@Valid @RequestBody SalvarRegraRequest request) {
        return RegraResponse.from(service.salvar(tenantContext.tenantId(),
                tenantContext.usuarioIdOuNulo(), request.comando()));
    }

    @GetMapping("/resolver")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public RegraResponse resolver(@RequestParam String tipoOperacao,
                                  @RequestParam String modelo,
                                  @RequestParam String regimeTributario,
                                  @RequestParam String ufDestino) {
        return RegraResponse.from(service.resolver(tenantContext.tenantId(),
                tipoOperacao, modelo, regimeTributario, ufDestino));
    }

    public record SalvarRegraRequest(@NotBlank String tipoOperacao, @NotBlank String modelo,
                                     @NotBlank String regimeTributario, @NotBlank String ufDestino,
                                     @NotBlank String cfop, String cstIcms, String csosn) {
        FiscalRegraOperacaoApplicationService.Comando comando() {
            return new FiscalRegraOperacaoApplicationService.Comando(
                    tipoOperacao, modelo, regimeTributario, ufDestino, cfop, cstIcms, csosn);
        }
    }

    public record RegraResponse(UUID id, String tipoOperacao, String modelo,
                                String regimeTributario, String ufDestino, String cfop,
                                String cstIcms, String csosn) {
        static RegraResponse from(FiscalRegraOperacaoApplicationService.Regra r) {
            return new RegraResponse(r.id(), r.tipoOperacao(), r.modelo(), r.regimeTributario(),
                    r.ufDestino(), r.cfop(), r.cstIcms(), r.csosn());
        }
    }
}
