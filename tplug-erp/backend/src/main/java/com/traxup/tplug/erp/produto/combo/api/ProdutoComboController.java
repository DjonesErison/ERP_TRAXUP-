package com.traxup.tplug.erp.produto.combo.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.produto.combo.ProdutoComboApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produtos/{produtoId}/combo")
public class ProdutoComboController {
    private final ProdutoComboApplicationService service;
    private final AuditoriaApplicationService auditoria;
    private final TenantContext tenantContext;

    public ProdutoComboController(ProdutoComboApplicationService service,
                                  AuditoriaApplicationService auditoria,
                                  TenantContext tenantContext) {
        this.service = service;
        this.auditoria = auditoria;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public List<ProdutoComboComponenteResponse> listar(@PathVariable UUID produtoId) {
        return service.listar(tenantContext.tenantId(), produtoId).stream()
                .map(ProdutoComboComponenteResponse::from)
                .toList();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public List<ProdutoComboComponenteResponse> configurar(@PathVariable UUID produtoId,
                                                           @Valid @RequestBody ConfigurarProdutoComboRequest request) {
        UUID tenantId = tenantContext.tenantId();
        var configuracoes = request.componentes().stream()
                .map(c -> new ProdutoComboApplicationService.ComponenteConfig(c.produtoId(), c.quantidade()))
                .toList();
        var componentes = service.configurar(tenantId, produtoId, configuracoes);
        auditoria.registrar(tenantId, tenantContext.usuarioIdOuNulo(), null, null,
                "CONFIGURAR", "PRODUTO_COMBO", produtoId, "componentes=" + componentes.size());
        return componentes.stream().map(ProdutoComboComponenteResponse::from).toList();
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ResponseEntity<Void> remover(@PathVariable UUID produtoId) {
        UUID tenantId = tenantContext.tenantId();
        service.remover(tenantId, produtoId);
        auditoria.registrar(tenantId, tenantContext.usuarioIdOuNulo(), null, null,
                "REMOVER_CONFIGURACAO", "PRODUTO_COMBO", produtoId, null);
        return ResponseEntity.noContent().build();
    }
}
