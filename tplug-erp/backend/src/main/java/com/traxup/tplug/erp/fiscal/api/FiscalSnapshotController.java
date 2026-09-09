package com.traxup.tplug.erp.fiscal.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.fiscal.FiscalSnapshotApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/solicitacoes")
public class FiscalSnapshotController {
    private final FiscalSnapshotApplicationService service;
    private final TenantContext tenantContext;

    public FiscalSnapshotController(FiscalSnapshotApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{solicitacaoId}/snapshot-itens")
    @PreAuthorize("hasAuthority('FISCAL_DOCUMENTO_EMITIR')")
    public FiscalSnapshotResponse gerar(@PathVariable UUID solicitacaoId) {
        var resultado = service.gerar(
                tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), solicitacaoId);
        return new FiscalSnapshotResponse(resultado.quantidadeItens(), resultado.repetida());
    }

    public record FiscalSnapshotResponse(int quantidadeItens, boolean repetida) {}
}
