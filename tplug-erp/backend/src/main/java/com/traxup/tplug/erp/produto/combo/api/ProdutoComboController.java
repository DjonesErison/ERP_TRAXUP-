package com.traxup.tplug.erp.produto.combo.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.produto.combo.ProdutoComboApplicationService;
import com.traxup.tplug.erp.produto.combo.ProdutoComboDisponibilidadeService;
import com.traxup.tplug.erp.produto.combo.ProdutoComboVigenciaApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produtos/{produtoId}/combo")
public class ProdutoComboController {
    private final ProdutoComboApplicationService service;
    private final ProdutoComboDisponibilidadeService disponibilidadeService;
    private final ProdutoComboVigenciaApplicationService vigenciaService;
    private final AuditoriaApplicationService auditoria;
    private final TenantContext tenantContext;

    public ProdutoComboController(ProdutoComboApplicationService service,
                                  ProdutoComboDisponibilidadeService disponibilidadeService,
                                  ProdutoComboVigenciaApplicationService vigenciaService,
                                  AuditoriaApplicationService auditoria,
                                  TenantContext tenantContext) {
        this.service = service;
        this.disponibilidadeService = disponibilidadeService;
        this.vigenciaService = vigenciaService;
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

    @GetMapping("/disponibilidade")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public ProdutoComboDisponibilidadeResponse disponibilidade(@PathVariable UUID produtoId,
                                                               @RequestParam UUID filialId) {
        var quantidade = disponibilidadeService.calcular(tenantContext.tenantId(), filialId, produtoId);
        return new ProdutoComboDisponibilidadeResponse(produtoId, filialId, quantidade);
    }

    @GetMapping("/vigencia")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public ResponseEntity<ProdutoComboVigenciaResponse> buscarVigencia(@PathVariable UUID produtoId) {
        return vigenciaService.buscar(tenantContext.tenantId(), produtoId)
                .map(ProdutoComboVigenciaResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
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

    @PutMapping("/vigencia")
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ProdutoComboVigenciaResponse configurarVigencia(@PathVariable UUID produtoId,
                                                            @RequestBody ConfigurarProdutoComboVigenciaRequest request) {
        UUID tenantId = tenantContext.tenantId();
        var vigencia = vigenciaService.configurar(
                tenantId, produtoId, request.vigenciaInicio(), request.vigenciaFim());
        auditoria.registrar(tenantId, tenantContext.usuarioIdOuNulo(), null, null,
                "CONFIGURAR_VIGENCIA", "PRODUTO_COMBO", produtoId,
                "inicio=" + request.vigenciaInicio() + ";fim=" + request.vigenciaFim());
        return ProdutoComboVigenciaResponse.from(vigencia);
    }

    @DeleteMapping("/vigencia")
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ResponseEntity<Void> removerVigencia(@PathVariable UUID produtoId) {
        UUID tenantId = tenantContext.tenantId();
        vigenciaService.remover(tenantId, produtoId);
        auditoria.registrar(tenantId, tenantContext.usuarioIdOuNulo(), null, null,
                "REMOVER_VIGENCIA", "PRODUTO_COMBO", produtoId, null);
        return ResponseEntity.noContent().build();
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
