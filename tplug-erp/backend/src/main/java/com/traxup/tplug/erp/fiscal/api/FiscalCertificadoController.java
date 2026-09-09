package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalCertificadoApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/certificados/filiais")
public class FiscalCertificadoController {
    private final FiscalCertificadoApplicationService service;
    private final TenantContext tenantContext;

    public FiscalCertificadoController(FiscalCertificadoApplicationService service,
                                       TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{filialId}")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_LER')")
    public FiscalCertificadoResponse buscar(@PathVariable UUID filialId) {
        return FiscalCertificadoResponse.from(
                service.buscarAtivo(tenantContext.tenantId(), filialId));
    }

    @PutMapping("/{filialId}")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalCertificadoResponse salvar(@PathVariable UUID filialId,
                                            @Valid @RequestBody SalvarFiscalCertificadoRequest request) {
        return FiscalCertificadoResponse.from(service.salvar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), filialId,
                request.tipo(), request.titular(), request.documentoTitular(),
                request.numeroSerie(), request.thumbprintSha256(),
                request.validadeInicio(), request.validadeFim(),
                request.cofreSegredos(), request.referenciaSegredo()));
    }

    public record FiscalCertificadoResponse(
            UUID id,
            UUID filialId,
            String tipo,
            String titular,
            String documentoTitularMascarado,
            String numeroSerie,
            String thumbprintMascarado,
            OffsetDateTime validadeInicio,
            OffsetDateTime validadeFim,
            String cofreSegredos,
            boolean ativo,
            boolean repetida
    ) {
        static FiscalCertificadoResponse from(
                FiscalCertificadoApplicationService.Resultado resultado) {
            return new FiscalCertificadoResponse(
                    resultado.id(), resultado.filialId(), resultado.tipo(),
                    resultado.titular(), resultado.documentoTitularMascarado(),
                    resultado.numeroSerie(), resultado.thumbprintMascarado(),
                    resultado.validadeInicio(), resultado.validadeFim(),
                    resultado.cofreSegredos(), resultado.ativo(), resultado.repetida());
        }
    }
}
